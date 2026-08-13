package com.sportsequipment.controller;

import com.sportsequipment.dto.AuthTokenResponse;
import com.sportsequipment.dto.LoginRequest;
import com.sportsequipment.dto.MessageResponse;
import com.sportsequipment.dto.RegisterRequest;
import com.sportsequipment.entity.User;
import com.sportsequipment.mapper.UserMapper;
import com.sportsequipment.security.JwtAuthTokenFilter;
import com.sportsequipment.security.JwtUtils;
import com.sportsequipment.security.UserDetailsImpl;
import com.sportsequipment.util.PasswordValidator;
import com.sportsequipment.util.RedisUtil;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * 认证控制器，处理用户登录、注册、刷新令牌、退出登录等认证相关功能
 *
 * @author system
 */
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    /**
     * 用户当前有效 refresh_token 存储：
     *   auth:refresh:user:{userId} = refreshJti  TTL = refreshTokenExpirationMs
     * 使用 userId 做 key 的好处：单点登录 / 强制下线 / 修改密码时，直接删这个 key 就能让所有设备 refresh 失效
     */
    public static final String REFRESH_USER_PREFIX = "auth:refresh:user:";
    /**
     * refresh_token 黑名单（主动退出时记录）：
     *   auth:refresh:blacklist:{refreshJti} = 1  TTL = 剩余有效期
     */
    public static final String REFRESH_BLACKLIST_PREFIX = "auth:refresh:blacklist:";

    private final AuthenticationManager authenticationManager;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final RedisUtil redisUtil;

    public AuthController(AuthenticationManager authenticationManager,
            UserMapper userMapper,
            PasswordEncoder passwordEncoder,
            JwtUtils jwtUtils,
            RedisUtil redisUtil) {
        this.authenticationManager = authenticationManager;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
        this.redisUtil = redisUtil;
    }

    /**
     * 临时密码重置接口 - 仅用于开发测试
     * 注意：生产环境必须删除此接口或添加严格的权限控制
     */
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        logger.info("收到密码重置请求，用户名: {}", request.getUsername());
        User user = userMapper.findByUsername(request.getUsername());
        if (user != null) {
            logger.info("找到用户: {}，执行密码重置", request.getUsername());
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
            user.setUpdatedAt(LocalDateTime.now());
            userMapper.update(user);
            // 密码重置后让该用户所有 refresh_token 失效，强制各端重新登录
            redisUtil.delete(REFRESH_USER_PREFIX + user.getId());
            logger.info("用户: {} 密码重置成功，已清理历史 refresh_token", request.getUsername());
            return ResponseEntity.ok(new MessageResponse("密码重置成功"));
        } else {
            logger.warn("密码重置失败：用户不存在，用户名: {}", request.getUsername());
            return ResponseEntity.badRequest().body(new MessageResponse("用户不存在"));
        }
    }

    /**
     * 重置密码请求类
     */
    @Getter
    @Setter
    public static class ResetPasswordRequest {
        private String username;
        private String newPassword;
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        logger.info("收到登录请求，用户名: {}", loginRequest.getUsername());

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            String role = userDetails.getRole();

            String accessToken = jwtUtils.generateAccessToken(authentication);
            String refreshToken = jwtUtils.generateRefreshToken(authentication);
            String refreshJti = jwtUtils.getJtiFromJwtToken(refreshToken);

            // 写入用户当前 refresh_token（TTL 与 refresh token 一致）
            redisUtil.set(REFRESH_USER_PREFIX + userDetails.getId(),
                    refreshJti,
                    jwtUtils.getRefreshTokenExpirationMs(),
                    TimeUnit.MILLISECONDS);

            logger.info("用户: {} 登录成功，角色: {}, refreshJti: {}", loginRequest.getUsername(), role, refreshJti);

            return ResponseEntity.ok(AuthTokenResponse.of(
                    accessToken,
                    refreshToken,
                    jwtUtils.getAccessTokenExpirationMs() / 1000L,
                    jwtUtils.getRefreshTokenExpirationMs() / 1000L,
                    userDetails.getId(),
                    userDetails.getUsername(),
                    userDetails.getEmail(),
                    role,
                    userDetails.getAvatar()));
        } catch (AuthenticationException e) {
            logger.error("用户: {} 登录失败: {}", loginRequest.getUsername(), e.getMessage());
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("登录失败: 用户名或密码错误"));
        }
    }

    /**
     * 刷新令牌
     * 请求体: { "refreshToken": "xxx" }
     * 采用 rotation 策略：每次刷新同时下发新的 refresh_token，旧 refresh_token 立刻拉黑防重放
     */
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenRequest request) {
        String refreshToken = request != null ? request.getRefreshToken() : null;
        if (!StringUtils.hasText(refreshToken)) {
            return ResponseEntity.badRequest().body(new MessageResponse("refresh_token 不能为空"));
        }
        try {
            if (!jwtUtils.validateJwtToken(refreshToken)) {
                return ResponseEntity.status(401).body(new MessageResponse("refresh_token 无效或已过期"));
            }
            if (!jwtUtils.isRefreshToken(refreshToken)) {
                return ResponseEntity.badRequest().body(new MessageResponse("token 类型错误，需要 refresh_token"));
            }
            String refreshJti = jwtUtils.getJtiFromJwtToken(refreshToken);
            // 是否被主动拉黑（用户退出）
            if (redisUtil.hasKey(REFRESH_BLACKLIST_PREFIX + refreshJti)) {
                logger.warn("refresh_token(jti={}) 已在黑名单中", refreshJti);
                return ResponseEntity.status(401).body(new MessageResponse("refresh_token 已失效，请重新登录"));
            }
            String username = jwtUtils.getUserNameFromJwtToken(refreshToken);
            User user = userMapper.findByUsername(username);
            if (user == null) {
                return ResponseEntity.status(401).body(new MessageResponse("用户不存在，请重新登录"));
            }
            // 对比：必须是该用户当前绑定的 refresh_token，否则属于已被轮换/作废旧 refresh
            String bindJti = (String) redisUtil.get(REFRESH_USER_PREFIX + user.getId());
            if (!refreshJti.equals(bindJti)) {
                logger.warn("refresh_token(jti={}) 与用户绑定值不一致，疑似重放攻击", refreshJti);
                // 安全加固：发现疑似被盗用，直接让用户整个 refresh 失效，强制重新登录
                redisUtil.delete(REFRESH_USER_PREFIX + user.getId());
                return ResponseEntity.status(401).body(new MessageResponse("refresh_token 已被轮换，请重新登录"));
            }

            // rotation：生成新的 access + refresh，并让旧 refresh 立刻失效（拉黑 + 覆盖绑定）
            String newAccessToken = jwtUtils.generateAccessTokenForUsername(username);
            String newRefreshToken = jwtUtils.generateRefreshTokenForUsername(username);
            String newRefreshJti = jwtUtils.getJtiFromJwtToken(newRefreshToken);

            Date oldExp = jwtUtils.getExpirationFromJwtToken(refreshToken);
            long oldTtl = oldExp.getTime() - System.currentTimeMillis();
            if (oldTtl > 0) {
                redisUtil.set(REFRESH_BLACKLIST_PREFIX + refreshJti, "1", oldTtl, TimeUnit.MILLISECONDS);
            }
            redisUtil.set(REFRESH_USER_PREFIX + user.getId(),
                    newRefreshJti,
                    jwtUtils.getRefreshTokenExpirationMs(),
                    TimeUnit.MILLISECONDS);

            logger.debug("用户 {} 刷新令牌成功，旧refreshJti={}, 新refreshJti={}",
                    username, refreshJti, newRefreshJti);

            return ResponseEntity.ok(AuthTokenResponse.refreshOnly(
                    newAccessToken,
                    newRefreshToken,
                    jwtUtils.getAccessTokenExpirationMs() / 1000L,
                    jwtUtils.getRefreshTokenExpirationMs() / 1000L));
        } catch (Exception e) {
            logger.error("刷新令牌失败: {}", e.getMessage(), e);
            return ResponseEntity.status(401).body(new MessageResponse("refresh_token 无效，请重新登录"));
        }
    }

    /**
     * 用户主动退出登录
     * 1) 当前 access_token 按 jti 加入黑名单
     * 2) 该用户绑定的 refresh_token 同时拉黑并删除绑定，保证不能再刷新
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser(HttpServletRequest request) {
        String jwt = parseJwtFromRequest(request);
        if (jwt == null) {
            return ResponseEntity.badRequest().body(new MessageResponse("未找到登录凭证"));
        }
        try {
            String username = null;
            if (jwtUtils.validateJwtToken(jwt)) {
                // 拉黑当前 access_token
                String jti = jwtUtils.getJtiFromJwtToken(jwt);
                Date expiration = jwtUtils.getExpirationFromJwtToken(jwt);
                long ttlMs = expiration.getTime() - System.currentTimeMillis();
                if (ttlMs > 0) {
                    String key = JwtAuthTokenFilter.TOKEN_BLACKLIST_PREFIX + jti;
                    redisUtil.set(key, "1", ttlMs, TimeUnit.MILLISECONDS);
                    logger.info("用户退出登录，access_token(jti={}) 已加入黑名单，剩余TTL={}ms", jti, ttlMs);
                }
                username = jwtUtils.getUserNameFromJwtToken(jwt);
            }

            if (username != null) {
                User user = userMapper.findByUsername(username);
                if (user != null) {
                    String bindJti = (String) redisUtil.get(REFRESH_USER_PREFIX + user.getId());
                    if (StringUtils.hasText(bindJti)) {
                        // 拉一个最长7天的兜底（防止token无法解析expiration时永久存留概率极低的泄漏）
                        redisUtil.set(REFRESH_BLACKLIST_PREFIX + bindJti, "1",
                                jwtUtils.getRefreshTokenExpirationMs(), TimeUnit.MILLISECONDS);
                        logger.info("用户退出登录，refresh_token(jti={}) 已加入黑名单", bindJti);
                    }
                    redisUtil.delete(REFRESH_USER_PREFIX + user.getId());
                }
            }

            SecurityContextHolder.clearContext();
            return ResponseEntity.ok(new MessageResponse("退出登录成功"));
        } catch (Exception e) {
            logger.error("退出登录失败: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(new MessageResponse("退出登录失败"));
        }
    }

    private String parseJwtFromRequest(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");
        if (headerAuth != null && StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }
        return null;
    }

    @Getter
    @Setter
    public static class RefreshTokenRequest {
        private String refreshToken;
    }

    /**
     * 管理员注册接口 - 仅供特殊场景使用，生产环境应移除或添加额外安全措施
     */
    @PostMapping("/register-admin")
    public ResponseEntity<?> registerAdmin(@Valid @RequestBody RegisterRequest signUpRequest) {
        logger.info("收到管理员注册请求，用户名: {}", signUpRequest.getUsername());
        return registerUser(signUpRequest, "ADMIN", "Admin registered successfully!");
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest signUpRequest) {
        logger.info("收到用户注册请求，用户名: {}, 邮箱: {}", signUpRequest.getUsername(), signUpRequest.getEmail());
        return registerUser(signUpRequest, "USER", "User registered successfully!");
    }

    /**
     * 创建用户的通用方法
     *
     * @param signUpRequest  注册请求
     * @param role           用户角色
     * @param successMessage 成功消息
     * @return ResponseEntity
     */
    private ResponseEntity<?> registerUser(RegisterRequest signUpRequest, String role, String successMessage) {
        try {
            // 检查用户名是否已存在
            if (userMapper.existsByUsername(signUpRequest.getUsername()) > 0) {
                logger.warn("注册失败: 用户名 {} 已被使用", signUpRequest.getUsername());
                return ResponseEntity
                        .badRequest()
                        .body(new MessageResponse("错误: 用户名已被使用!"));
            }

            // 检查邮箱是否已被使用
            if (userMapper.existsByEmail(signUpRequest.getEmail()) > 0) {
                logger.warn("注册失败: 邮箱 {} 已被使用", signUpRequest.getEmail());
                return ResponseEntity
                        .badRequest()
                        .body(new MessageResponse("错误: 邮箱已被使用!"));
            }

            // 只有非普通用户才验证密码强度
            if (!"USER".equals(role) && !PasswordValidator.isValid(signUpRequest.getPassword())) {
                logger.warn("注册失败: 密码不符合安全要求");
                return ResponseEntity.badRequest().body(new MessageResponse(PasswordValidator.getValidationMessage()));
            }

            // 创建新用户账户
            User user = new User(signUpRequest.getUsername(),
                    signUpRequest.getEmail(),
                    passwordEncoder.encode(signUpRequest.getPassword()));

            user.setRole(role);
            user.setPhone(signUpRequest.getPhone());
            user.setAddress(signUpRequest.getAddress());
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());

            userMapper.insert(user);
            logger.info("用户注册成功: ID={}, 用户名={}, 角色={}", user.getId(), user.getUsername(), role);

            return ResponseEntity.ok(new MessageResponse(successMessage));
        } catch (Exception e) {
            logger.error("注册过程中发生错误: {}", e.getMessage(), e);
            return ResponseEntity
                    .internalServerError()
                    .body(new MessageResponse("注册失败: 内部服务器错误"));
        }
    }
}
