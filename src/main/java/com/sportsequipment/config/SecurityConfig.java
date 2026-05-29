package com.sportsequipment.config;

import com.sportsequipment.security.JwtAuthEntryPoint;
import com.sportsequipment.security.JwtAuthTokenFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Security配置类，处理认证和授权相关配置
 * 
 * @author System
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthEntryPoint unauthorizedHandler;

    @Value("${cors.allowed-origins}")
    private String allowedOrigins;

    public SecurityConfig(JwtAuthEntryPoint unauthorizedHandler) {
        this.unauthorizedHandler = unauthorizedHandler;
    }

    @Bean
    public JwtAuthTokenFilter authenticationJwtTokenFilter() {
        return new JwtAuthTokenFilter();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // 使用 DelegatingPasswordEncoder 支持多种密码编码方式
        Map<String, PasswordEncoder> encoders = new HashMap<>();
        encoders.put("noop", NoOpPasswordEncoder.getInstance());
        encoders.put("plain", new com.sportsequipment.security.PlainTextPasswordEncoder());

        DelegatingPasswordEncoder delegatingEncoder = new DelegatingPasswordEncoder("noop", encoders);
        // 设置默认编码器为 noop（明文），当密码没有前缀时使用
        delegatingEncoder.setDefaultPasswordEncoderForMatches(NoOpPasswordEncoder.getInstance());

        return delegatingEncoder;
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
                        .requestMatchers("/api/auth/login", "/api/auth/register", "/api/auth/register-admin")
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

    // 为了与WebMvcConfig中的CORS配置保持一致性，可以保留此配置方法
    // 但不再在securityFilterChain中显式调用它，而是使用Customizer.withDefaults()

    /**
     * CommandLineRunner用于在应用启动时生成密码哈希值
     * 用于管理员密码重置时获取正确的BCrypt哈希值
     */
    @Bean
    public CommandLineRunner passwordEncoderRunner() {
        return args -> {
            Logger logger = LoggerFactory.getLogger(SecurityConfig.class);
            PasswordEncoder encoder = passwordEncoder();

            // 生成示例密码的哈希值
            String plainPassword = "admin123";
            String encodedPassword = encoder.encode(plainPassword);

            logger.info("====================================");
            logger.info(String.format("原始密码: %s", plainPassword));
            logger.info(String.format("BCrypt哈希值: %s", encodedPassword));
            logger.info("用于SQL更新的语句:");
            logger.info(String.format("UPDATE user SET password = '%s' WHERE username = 'admin';", encodedPassword));
            logger.info("====================================");
        };
    }
}
