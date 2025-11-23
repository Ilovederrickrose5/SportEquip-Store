package com.sportsequipment.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.util.unit.DataSize;
import jakarta.servlet.MultipartConfigElement;
import lombok.Getter;

/**
 * 文件上传统一配置类
 * @author 30776
 */
@Configuration
@Getter
public class FileUploadConfig {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Value("${file.max-size}")
    private long maxFileSize;
    
    @Value("${server.url}")
    private String serverUrl;

    @Bean
    public MultipartResolver multipartResolver() {
        // 在Spring Boot 3.x中，使用StandardServletMultipartResolver替代CommonsMultipartResolver
        return new StandardServletMultipartResolver();
    }
    
    @Bean
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        // 设置文件大小限制
        factory.setMaxFileSize(DataSize.ofBytes(maxFileSize));
        factory.setMaxRequestSize(DataSize.ofBytes(maxFileSize * 2)); 
        // 请求总大小限制为单个文件的两倍
        return factory.createMultipartConfig();
    }
}