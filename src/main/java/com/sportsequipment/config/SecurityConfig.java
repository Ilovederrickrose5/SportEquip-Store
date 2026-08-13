package com.sportsequipment.config;

import com.sportsequipment.security.JwtAuthEntryPoint;
import com.sportsequipment.security.JwtAuthTokenFilter;
import com.sportsequipment.security.JwtUtils;
import com.sportsequipment.security.UserDetailsServiceImpl;
import com.sportsequipment.util.RedisUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.Arrays;
import java.util.Collections;

/**
 * Security配置类，处理认证和授权相关配置
 * 
 * @author System
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthEntryPoint unauthorizedHandler;
    private final JwtUtils jwtUtils;
    private final UserDetailsServiceImpl userDetailsService;
    private final RedisUtil redisUtil;

    @Value("${cors.allowed-origins}")
    private String allowedOrigins;

    public SecurityConfig(JwtAuthEntryPoint unauthorizedHandler,
            JwtUtils jwtUtils,
            UserDetailsServiceImpl userDetailsService,
            RedisUtil redisUtil) {
        this.unauthorizedHandler = unauthorizedHandler;
        this.jwtUtils = jwtUtils;
        this.userDetailsService = userDetailsService;
        this.redisUtil = redisUtil;
    }

    @Bean
    public JwtAuthTokenFilter authenticationJwtTokenFilter() {
        return new JwtAuthTokenFilter(jwtUtils, userDetailsService, redisUtil);
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // 开发环境：明文密码比较（方便测试）
        // 使用自定义PasswordEncoder避免NoOpPasswordEncoder弃用警告
        return new PasswordEncoder() {
            @Override
            public String encode(CharSequence rawPassword) {
                return rawPassword.toString();
            }

            @Override
            public boolean matches(CharSequence rawPassword, String encodedPassword) {
                return rawPassword != null && rawPassword.equals(encodedPassword);
            }

            @Override
            public boolean upgradeEncoding(String encodedPassword) {
                return false;
            }
        };
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                // 配置CORS
                .cors(cors -> cors.configurationSource(request -> {
                    org.springframework.web.cors.CorsConfiguration config = new org.springframework.web.cors.CorsConfiguration();
                    // 从配置读取，支持多 origins（逗号分隔）
                    config.setAllowedOriginPatterns(Arrays.asList(allowedOrigins.split(",")));
                    config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                    config.setAllowedHeaders(Collections.singletonList("*"));
                    config.setExposedHeaders(Arrays.asList("Authorization", "Content-Type", "Accept"));
                    config.setAllowCredentials(true);
                    config.setMaxAge(3600L);
                    return config;
                }))
                .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        // 允许所有用户访问的路径（浏览功能）
                        .requestMatchers("/api/auth/login",
                                "/api/auth/register",
                                "/api/auth/register-admin",
                                "/api/auth/refresh")
                        .permitAll()
                        // 密码重置接口仅管理员可访问
                        .requestMatchers("/api/auth/reset-password").hasRole("ADMIN")
                        .requestMatchers("/api/products", "/api/products/**").permitAll()
                        .requestMatchers("/api/categories").permitAll()
                        .requestMatchers("/upload/**").permitAll()
                        .requestMatchers("/static/**").permitAll()
                        .requestMatchers("/images/**").permitAll()
                        .requestMatchers("/assets/**").permitAll()
                        .requestMatchers("/vite.svg").permitAll()

                        // 需要认证的路径（操作功能）
                        .requestMatchers("/api/upload/**").authenticated()
                        .requestMatchers("/api/files/**").authenticated()

                        // 用户相关的认证后路径
                        .requestMatchers("/api/users/me", "/api/users/change-password", "/api/users/*/role")
                        .authenticated()
                        // 管理员才能访问的路径 - 添加/api/users以支持用户管理
                        .requestMatchers("/api/admin/**", "/api/users").hasRole("ADMIN")

                        // 其他需要认证的请求
                        .anyRequest().authenticated())
                .addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
