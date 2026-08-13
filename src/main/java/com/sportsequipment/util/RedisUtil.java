package com.sportsequipment.util;

import org.redisson.api.RBucket;
import org.redisson.api.RKeys;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Component
public class RedisUtil {

    private final RedissonClient redissonClient;

    public RedisUtil(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    public void set(String key, Object value) {
        validateKey(key);
        RBucket<Object> bucket = redissonClient.getBucket(key);
        bucket.set(value);
    }

    public void set(String key, Object value, long timeout, TimeUnit unit) {
        validateKey(key);
        validateTimeout(timeout);
        RBucket<Object> bucket = redissonClient.getBucket(key);
        bucket.set(value, Duration.ofMillis(unit.toMillis(timeout)));
    }

    public void set(String key, Object value, Duration duration) {
        validateKey(key);
        RBucket<Object> bucket = redissonClient.getBucket(key);
        bucket.set(value, duration);
    }

    /**
     * 原子 SETNX：只有 key 不存在时才写入并设置过期。
     * 
     * @return true 写入成功（key 原本不存在）；false 已存在未写入
     */
    @SuppressWarnings("deprecation")
    public Boolean setIfAbsent(String key, Object value, long timeout, TimeUnit unit) {
        validateKey(key);
        validateTimeout(timeout);
        RBucket<Object> bucket = redissonClient.getBucket(key);
        // Redisson 3.26：(V, long, TimeUnit) 三参仍可正常工作但已标记 deprecated；
        // 新项目推荐的 (V, Duration) 双参重载在当前 3.26.0 版本的 RBucket 上实际不存在，
        // 调用会直接编译失败，因此保留稳定的三参签名并压制 deprecation 警告。
        return bucket.trySet(value, timeout, unit);
    }

    public <T> T get(String key, Class<T> clazz) {
        validateKey(key);
        RBucket<Object> bucket = redissonClient.getBucket(key);
        Object value = bucket.get();
        if (value == null) {
            return null;
        }
        return clazz.cast(value);
    }

    public Object get(String key) {
        validateKey(key);
        RBucket<Object> bucket = redissonClient.getBucket(key);
        return bucket.get();
    }

    public boolean delete(String key) {
        validateKey(key);
        return redissonClient.getBucket(key).delete();
    }

    public boolean hasKey(String key) {
        validateKey(key);
        return redissonClient.getBucket(key).isExists();
    }

    public boolean expire(String key, long timeout, TimeUnit unit) {
        validateKey(key);
        validateTimeout(timeout);
        return redissonClient.getBucket(key).expire(Duration.ofMillis(unit.toMillis(timeout)));
    }

    public boolean expire(String key, Duration duration) {
        validateKey(key);
        return redissonClient.getBucket(key).expire(duration);
    }

    public long getExpire(String key) {
        validateKey(key);
        Long ttl = redissonClient.getBucket(key).remainTimeToLive();
        return ttl != null ? ttl : 0L;
    }

    public void deletePattern(String pattern) {
        validateKey(pattern);
        RKeys keys = redissonClient.getKeys();
        Iterable<String> matchingKeys = keys.getKeysByPattern(pattern);
        for (String key : matchingKeys) {
            redissonClient.getBucket(key).delete();
        }
    }

    public Set<String> keys(String pattern) {
        validateKey(pattern);
        Set<String> result = new HashSet<>();
        RKeys keys = redissonClient.getKeys();
        Iterable<String> matchingKeys = keys.getKeysByPattern(pattern);
        matchingKeys.forEach(result::add);
        return result;
    }

    public RLock getLock(String key) {
        validateKey(key);
        return redissonClient.getLock(key);
    }

    public boolean tryLock(String key, long waitTime, long leaseTime, TimeUnit unit) {
        validateKey(key);
        try {
            return redissonClient.getLock(key).tryLock(waitTime, leaseTime, unit);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    public void unlock(String key) {
        validateKey(key);
        RLock lock = redissonClient.getLock(key);
        if (lock.isHeldByCurrentThread()) {
            lock.unlock();
        }
    }

    public void lock(String key) {
        validateKey(key);
        redissonClient.getLock(key).lock();
    }

    public void lock(String key, long leaseTime, TimeUnit unit) {
        validateKey(key);
        redissonClient.getLock(key).lock(leaseTime, unit);
    }

    private void validateKey(String key) {
        if (key == null || key.trim().isEmpty()) {
            throw new IllegalArgumentException("Redis key cannot be null or empty");
        }
    }

    private void validateTimeout(long timeout) {
        if (timeout <= 0) {
            throw new IllegalArgumentException("Timeout must be greater than 0");
        }
    }
}
