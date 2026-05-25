package com.sportsequipment.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 服务器配置属性
 */
@Configuration
@ConfigurationProperties(prefix = "server")
public class ServerProperties {
    
    private String url;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}