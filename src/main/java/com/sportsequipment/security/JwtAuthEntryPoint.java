package com.sportsequipment.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class JwtAuthEntryPoint implements AuthenticationEntryPoint {
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthEntryPoint.class);

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        logger.error("未授权错误: {}", authException.getMessage());
        if (authException.getMessage() != null && authException.getMessage().contains("Bad credentials")) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "错误: 用户名或密码不匹配");
        } else {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "错误: 未授权访问");
        }
    }
}
    