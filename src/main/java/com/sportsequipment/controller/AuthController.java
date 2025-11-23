package com.sportsequipment.controller;

import com.sportsequipment.dto.JwtResponse;
import com.sportsequipment.dto.LoginRequest;
import com.sportsequipment.dto.MessageResponse;
import com.sportsequipment.dto.RegisterRequest;
import com.sportsequipment.entity.User;
import com.sportsequipment.repository.UserRepository;
import com.sportsequipment.security.JwtUtils;
import com.sportsequipment.security.UserDetailsImpl;
import com.sportsequipment.util.PasswordValidator;
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
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * 认证控制器，处理用户登录、注册等认证相关功能
 * @author system
 */
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    
    public AuthController(AuthenticationManager authenticationManager, 
                         UserRepository userRepository, 
                         PasswordEncoder passwordEncoder,
                         JwtUtils jwtUtils) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
    }
    
    /**
     * 临时密码重置接口 - 仅用于开发测试
     * 注意：生产环境必须删除此接口或添加严格的权限控制
     */
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        logger.info("收到密码重置请求，用户名: {}", request.getUsername());
        return userRepository.findByUsername(request.getUsername())
                .map(user -> {
                    logger.info("找到用户: {}，执行密码重置", request.getUsername());
                    user.setPassword(passwordEncoder.encode(request.getNewPassword()));
                    userRepository.save(user);
                    logger.info("用户: {} 密码重置成功", request.getUsername());
                    return ResponseEntity.ok(new MessageResponse("密码重置成功"));
                })
                .orElseGet(() -> {
                    logger.warn("密码重置失败：用户不存在，用户名: {}", request.getUsername());
                    return ResponseEntity.badRequest().body(new MessageResponse("用户不存在"));
                });
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
            String jwt = jwtUtils.generateJwtToken(authentication);

            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            String role = userDetails.getRole();
            // 获取用户角色
            
            logger.info("用户: {} 登录成功，角色: {}", loginRequest.getUsername(), role);

            return ResponseEntity.ok(new JwtResponse(jwt,
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
     * @param signUpRequest 注册请求
     * @param role 用户角色
     * @param successMessage 成功消息
     * @return ResponseEntity
     */
    private ResponseEntity<?> registerUser(RegisterRequest signUpRequest, String role, String successMessage) {
        try {
            // 检查用户名是否已存在
            if (userRepository.existsByUsername(signUpRequest.getUsername())) {
                logger.warn("注册失败: 用户名 {} 已被使用", signUpRequest.getUsername());
                return ResponseEntity
                        .badRequest()
                        .body(new MessageResponse("错误: 用户名已被使用!"));
            }

            // 检查邮箱是否已被使用
            if (userRepository.existsByEmail(signUpRequest.getEmail())) {
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

            User savedUser = userRepository.save(user);
            logger.info("用户注册成功: ID={}, 用户名={}, 角色={}", savedUser.getId(), savedUser.getUsername(), role);

            return ResponseEntity.ok(new MessageResponse(successMessage));
        } catch (Exception e) {
            logger.error("注册过程中发生错误: {}", e.getMessage(), e);
            return ResponseEntity
                    .internalServerError()
                    .body(new MessageResponse("注册失败: 内部服务器错误"));
        }
    }
}
    