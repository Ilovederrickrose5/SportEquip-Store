package com.sportsequipment.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 全局自定义配置注册：
 * 1) 为 VSCode Spring Boot Language Server 提供属性元数据，消除 application.properties
 *    中 "unknown property" 警告；
 * 2) 代码中可通过构造函数注入直接读取这些配置（类型安全）。
 */
@Configuration
@EnableConfigurationProperties({
        SportEquipmentProperties.AppProperties.class,
        SportEquipmentProperties.MqProperties.class
})
public class SportEquipmentProperties {

    @ConfigurationProperties(prefix = "sportsequipment.app")
    public static class AppProperties {
        /** 兼容旧版单 token 默认 24 小时过期（ms） */
        private long jwtExpirationMs = 86400000L;
        /** access_token 过期时间（ms），默认 15 分钟 */
        private long accessTokenExpirationMs = 900000L;
        /** refresh_token 过期时间（ms），默认 7 天 */
        private long refreshTokenExpirationMs = 604800000L;

        public long getJwtExpirationMs() { return jwtExpirationMs; }
        public void setJwtExpirationMs(long jwtExpirationMs) { this.jwtExpirationMs = jwtExpirationMs; }
        public long getAccessTokenExpirationMs() { return accessTokenExpirationMs; }
        public void setAccessTokenExpirationMs(long accessTokenExpirationMs) { this.accessTokenExpirationMs = accessTokenExpirationMs; }
        public long getRefreshTokenExpirationMs() { return refreshTokenExpirationMs; }
        public void setRefreshTokenExpirationMs(long refreshTokenExpirationMs) { this.refreshTokenExpirationMs = refreshTokenExpirationMs; }
    }

    @ConfigurationProperties(prefix = "sportsequipment.mq")
    public static class MqProperties {
        /** PENDING 订单未支付超时自动取消 TTL（ms），默认 30 分钟 */
        private long orderPendingTtlMs = 1800000L;
        /** MQ 消费者幂等 Redis key TTL（秒），默认 24 小时 */
        private int idempotentTtlSeconds = 86400;

        public long getOrderPendingTtlMs() { return orderPendingTtlMs; }
        public void setOrderPendingTtlMs(long orderPendingTtlMs) { this.orderPendingTtlMs = orderPendingTtlMs; }
        public int getIdempotentTtlSeconds() { return idempotentTtlSeconds; }
        public void setIdempotentTtlSeconds(int idempotentTtlSeconds) { this.idempotentTtlSeconds = idempotentTtlSeconds; }
    }
}
