package com.sportsequipment.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.spring.cache.CacheConfig;
import org.redisson.spring.cache.RedissonSpringCacheManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching
public class RedisConfig {

    @Value("${spring.data.redis.host:localhost}")
    private String redisHost;

    @Value("${spring.data.redis.port:6379}")
    private int redisPort;

    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient() {
        Config config = new Config();
        config.useSingleServer()
                .setAddress("redis://" + redisHost + ":" + redisPort);
        return Redisson.create(config);
    }

    /**
     * 配置 Spring Cache 各缓存名称的 TTL 过期时间
     * 避免商品列表/详情缓存永不过期，导致内存无限增长
     *
     * CacheConfig 参数说明：
     * - 第一个参数：TTL（存活时间，毫秒）
     * - 第二个参数：maxIdleTime（最大空闲时间，毫秒）
     */
    @Bean
    public RedissonSpringCacheManager cacheManager(RedissonClient redissonClient) {
        Map<String, CacheConfig> configMap = new HashMap<>();

        // 商品列表缓存：60 分钟 TTL，30 分钟最大空闲
        configMap.put("product:list", new CacheConfig(60 * 60 * 1000L, 30 * 60 * 1000L));

        // 商品详情缓存：60 分钟 TTL，30 分钟最大空闲
        configMap.put("product:detail", new CacheConfig(60 * 60 * 1000L, 30 * 60 * 1000L));

        // 分类缓存：120 分钟 TTL，60 分钟最大空闲（分类变动较少）
        // category:list - 全部分类树缓存；category:main/sub/third - 兼容旧接口的单条缓存
        // category:detail - 新统一 Category 实体单条详情缓存
        configMap.put("category:list", new CacheConfig(120 * 60 * 1000L, 60 * 60 * 1000L));
        configMap.put("category:main", new CacheConfig(120 * 60 * 1000L, 60 * 60 * 1000L));
        configMap.put("category:sub", new CacheConfig(120 * 60 * 1000L, 60 * 60 * 1000L));
        configMap.put("category:third", new CacheConfig(120 * 60 * 1000L, 60 * 60 * 1000L));
        configMap.put("category:detail", new CacheConfig(120 * 60 * 1000L, 60 * 60 * 1000L));

        return new RedissonSpringCacheManager(redissonClient, configMap);
    }
}
