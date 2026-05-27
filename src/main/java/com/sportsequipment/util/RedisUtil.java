package com.sportsequipment.util;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Component
public class RedisUtil {

    private final RedisTemplate<String, Object> objectRedisTemplate;
    private final StringRedisTemplate stringRedisTemplate;

    public RedisUtil(RedisTemplate<String, Object> objectRedisTemplate, StringRedisTemplate stringRedisTemplate) {
        this.objectRedisTemplate = Objects.requireNonNull(objectRedisTemplate, "objectRedisTemplate must not be null");
        this.stringRedisTemplate = Objects.requireNonNull(stringRedisTemplate, "stringRedisTemplate must not be null");
    }

    public void set(String key, Object value) {
        validateKey(key);
        objectRedisTemplate.opsForValue().set(key, value);
    }

    public void set(String key, Object value, long timeout, TimeUnit unit) {
        validateKey(key);
        validateTimeout(timeout);
        Objects.requireNonNull(unit, "TimeUnit must not be null");
        final TimeUnit finalUnit = unit;
        objectRedisTemplate.opsForValue().set(key, value, timeout, finalUnit);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> clazz) {
        validateKey(key);
        Object value = objectRedisTemplate.opsForValue().get(key);
        if (value == null) {
            return null;
        }
        return clazz.cast(value);
    }

    public Object get(String key) {
        validateKey(key);
        return objectRedisTemplate.opsForValue().get(key);
    }

    public boolean delete(String key) {
        validateKey(key);
        Boolean result = objectRedisTemplate.delete(key);
        return Boolean.TRUE.equals(result);
    }

    public boolean hasKey(String key) {
        validateKey(key);
        Boolean result = objectRedisTemplate.hasKey(key);
        return Boolean.TRUE.equals(result);
    }

    public boolean expire(String key, long timeout, TimeUnit unit) {
        validateKey(key);
        validateTimeout(timeout);
        Objects.requireNonNull(unit, "TimeUnit must not be null");
        final TimeUnit finalUnit = unit;
        Boolean result = objectRedisTemplate.expire(key, timeout, finalUnit);
        return Boolean.TRUE.equals(result);
    }

    public long getExpire(String key) {
        validateKey(key);
        Long result = objectRedisTemplate.getExpire(key);
        return result != null ? result : 0L;
    }

    @SuppressWarnings("unchecked")
    public void deletePattern(String pattern) {
        validateKey(pattern);
        Set<String> keys = new HashSet<>();
        Set<?> rawKeys = objectRedisTemplate.keys(pattern);
        if (rawKeys != null) {
            for (Object key : rawKeys) {
                keys.add(String.valueOf(key));
            }
        }
        if (!keys.isEmpty()) {
            objectRedisTemplate.delete(keys);
        }
    }

    public boolean tryLock(String key, String value, long expireTime, TimeUnit unit) {
        validateKey(key);
        validateValue(value);
        validateTimeout(expireTime);
        Objects.requireNonNull(unit, "TimeUnit must not be null");
        final TimeUnit finalUnit = unit;
        Boolean result = stringRedisTemplate.opsForValue().setIfAbsent(key, value, expireTime, finalUnit);
        return Boolean.TRUE.equals(result);
    }

    public boolean unlock(String key, String value) {
        validateKey(key);
        validateValue(value);
        String currentValue = stringRedisTemplate.opsForValue().get(key);
        if (value.equals(currentValue)) {
            return Boolean.TRUE.equals(stringRedisTemplate.delete(key));
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
