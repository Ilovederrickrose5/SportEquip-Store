package com.sportsequipment.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 应用自定义配置属性
 */
@Configuration
@ConfigurationProperties(prefix = "sportsequipment.app")
public class AppProperties {

  private String jwtSecret;
  private Long jwtExpirationMs;

  public String getJwtSecret() {
    return jwtSecret;
  }

  public void setJwtSecret(String jwtSecret) {
    this.jwtSecret = jwtSecret;
  }

  public Long getJwtExpirationMs() {
    return jwtExpirationMs;
  }

  public void setJwtExpirationMs(Long jwtExpirationMs) {
    this.jwtExpirationMs = jwtExpirationMs;
  }
}