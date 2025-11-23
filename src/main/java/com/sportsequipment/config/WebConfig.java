package com.sportsequipment.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web配置类，处理静态资源和CORS配置
 * 
 * @author System
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final FileUploadConfig fileUploadConfig;

    public WebConfig(FileUploadConfig fileUploadConfig) {
        this.fileUploadConfig = fileUploadConfig;
    }

    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        // 配置静态资源处理器，映射/upload/**到文件系统的上传目录，支持子目录
        registry.addResourceHandler("/upload/**")
                .addResourceLocations("file:" + fileUploadConfig.getUploadDir() + "/")
                .setCachePeriod(0); // 开发环境设置为0，生产环境可以设置更大的值
    }

    @Override
    public void addCorsMappings(@NonNull CorsRegistry registry) {
        // CORS配置已移至SecurityConfig中
    }
}