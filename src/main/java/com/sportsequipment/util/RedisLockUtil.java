package com.sportsequipment.util;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Component
@SuppressWarnings("unchecked")
public class RedisLockUtil {

    private final StringRedisTemplate stringRedisTemplate;

    private static final String LOCK_PREFIX = "lock:";
    private static final long DEFAULT_EXPIRE_TIME = 30L;

    public RedisLockUtil(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = Objects.requireNonNull(stringRedisTemplate, "stringRedisTemplate must not be null");
    }

    public boolean tryLock(String key, String value) {
        return tryLock(key, value, DEFAULT_EXPIRE_TIME, TimeUnit.SECONDS);
    }

    public boolean tryLock(String key, String value, long expireTime, TimeUnit unit) {
        validateKey(key);
        validateValue(value);
        validateTimeout(expireTime);
        Objects.requireNonNull(unit, "TimeUnit must not be null");

        String lockKey = LOCK_PREFIX + key;
        Boolean result = stringRedisTemplate.opsForValue().setIfAbsent(lockKey, value, expireTime, unit);
        return Boolean.TRUE.equals(result);
    }

    public boolean unlock(String key, String value) {
        validateKey(key);
        validateValue(value);

        String lockKey = LOCK_PREFIX + key;
        String currentValue = stringRedisTemplate.opsForValue().get(lockKey);
        if (value.equals(currentValue)) {
            return Boolean.TRUE.equals(stringRedisTemplate.delete(lockKey));
        }
        return false;
    }

    private void validateKey(String key) {
        if (key == null || key.trim().isEmpty()) {
            throw new IllegalArgumentException("Lock key cannot be null or empty");
        }
    }

    private void validateValue(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Lock value cannot be null or empty");
        }
    }

    private void validateTimeout(long timeout) {
        if (timeout <= 0) {
            throw new IllegalArgumentException("Lock expire time must be greater than 0");
        }
    }
}
