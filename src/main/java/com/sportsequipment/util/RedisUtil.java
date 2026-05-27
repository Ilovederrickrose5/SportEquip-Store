package com.sportsequipment.util;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Component
public class RedisUtil {

    private final RedisTemplate<String, Object> redisTemplate;

    public RedisUtil(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = Objects.requireNonNull(redisTemplate, "redisTemplate must not be null");
    }

    public void set(String key, Object value) {
        validateKey(key);
        redisTemplate.opsForValue().set(key, value);
    }

    public void set(String key, Object value, long timeout, TimeUnit unit) {
        validateKey(key);
        validateTimeout(timeout);
        Objects.requireNonNull(unit, "TimeUnit must not be null");
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    public Object get(String key) {
        validateKey(key);
        return redisTemplate.opsForValue().get(key);
    }

    public boolean delete(String key) {
        validateKey(key);
        Boolean result = redisTemplate.delete(key);
        return Boolean.TRUE.equals(result);
    }

    public boolean hasKey(String key) {
        validateKey(key);
        Boolean result = redisTemplate.hasKey(key);
        return Boolean.TRUE.equals(result);
    }

    public boolean expire(String key, long timeout, TimeUnit unit) {
        validateKey(key);
        validateTimeout(timeout);
        Objects.requireNonNull(unit, "TimeUnit must not be null");
        Boolean result = redisTemplate.expire(key, timeout, unit);
        return Boolean.TRUE.equals(result);
    }

    public long getExpire(String key) {
        validateKey(key);
        Long result = redisTemplate.getExpire(key);
        return result != null ? result : 0L;
    }

    @SuppressWarnings("unchecked")
    public void deletePattern(String pattern) {
        validateKey(pattern);
        Set<String> keys = redisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    public boolean tryLock(String key, String value, long expireTime, TimeUnit unit) {
        validateKey(key);
        validateValue(value);
        validateTimeout(expireTime);
        Objects.requireNonNull(unit, "TimeUnit must not be null");
        Boolean result = redisTemplate.opsForValue().setIfAbsent(key, value, expireTime, unit);
        return Boolean.TRUE.equals(result);
    }

    public boolean unlock(String key, String value) {
        validateKey(key);
        validateValue(value);
        Object currentValue = redisTemplate.opsForValue().get(key);
        if (value.equals(currentValue)) {
            return Boolean.TRUE.equals(redisTemplate.delete(key));
        }
        return false;
    }

    private void validateKey(String key) {
        if (key == null || key.trim().isEmpty()) {
            throw new IllegalArgumentException("Redis key cannot be null or empty");
        }
    }

    private void validateValue(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Redis value cannot be null or empty");
        }
    }

    private void validateTimeout(long timeout) {
        if (timeout <= 0) {
            throw new IllegalArgumentException("Timeout must be greater than 0");
        }
    }
}
