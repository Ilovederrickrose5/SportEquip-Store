package com.sportsequipment.security;

import com.sportsequipment.util.RedisUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.lang.NonNull;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

public class JwtAuthTokenFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final UserDetailsServiceImpl userDetailsService;
    private final RedisUtil redisUtil;

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthTokenFilter.class);
    public static final String TOKEN_BLACKLIST_PREFIX = "auth:blacklist:";

    public JwtAuthTokenFilter(JwtUtils jwtUtils,
            UserDetailsServiceImpl userDetailsService,
            RedisUtil redisUtil) {
        this.jwtUtils = jwtUtils;
        this.userDetailsService = userDetailsService;
        this.redisUtil = redisUtil;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        try {
            logger.debug("处理请求: {}", request.getRequestURI());

            String jwt = parseJwt(request);
            logger.debug("从请求头解析JWT: {}", jwt != null ? "已找到" : "未找到");

            if (jwt != null) {
                boolean isValid = jwtUtils.validateJwtToken(jwt);
                logger.debug("JWT验证结果: {}", isValid ? "有效" : "无效");

                if (isValid) {
                    if (!jwtUtils.isAccessToken(jwt)) {
                        logger.warn("使用 refresh_token 访问业务接口，拒绝认证");
                        SecurityContextHolder.clearContext();
                    } else if (isBlacklisted(jwt)) {
                        logger.warn("JWT已在黑名单中（用户已退出），拒绝访问");
                        SecurityContextHolder.clearContext();
                    } else {
                        String username = jwtUtils.getUserNameFromJwtToken(jwt);
                        logger.info("JWT验证成功，用户名: {}", username);

                        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        logger.debug("用户 {} 已成功认证，设置认证上下文", username);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("设置用户认证失败: {}", e.getMessage(), e);
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    private boolean isBlacklisted(String jwt) {
        try {
            String jti = jwtUtils.getJtiFromJwtToken(jwt);
            if (jti == null) {
                return false;
            }
            return redisUtil.hasKey(TOKEN_BLACKLIST_PREFIX + jti);
        } catch (Exception e) {
            logger.error("检查token黑名单失败: {}", e.getMessage());
            return false;
        }
    }

    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");

        logger.debug("Authorization头: {}", headerAuth != null ? "已存在" : "不存在");

        if (headerAuth != null && StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            logger.debug("Authorization头格式正确，开始提取JWT");
            return headerAuth.substring(7);
        } else if (headerAuth != null && StringUtils.hasText(headerAuth)) {
            logger.warn("Authorization头格式错误，应为 'Bearer {token}'");
        }

        return null;
    }
}
