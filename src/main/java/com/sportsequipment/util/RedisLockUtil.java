package com.sportsequipment.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class RedisLockUtil {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String LOCK_PREFIX = "lock:";
    private static final long DEFAULT_EXPIRE_TIME = 30;

    public boolean tryLock(String key, String value) {
        return tryLock(key, value, DEFAULT_EXPIRE_TIME, TimeUnit.SECONDS);
    }

    public boolean tryLock(String key, String value, long expireTime, TimeUnit unit) {
        String lockKey = LOCK_PREFIX + key;
        Boolean result = redisTemplate.opsForValue().setIfAbsent(lockKey, value, expireTime, unit);
        return Boolean.TRUE.equals(result);
    }

    public boolean unlock(String key, String value) {
        String lockKey = LOCK_PREFIX + key;
        Object currentValue = redisTemplate.opsForValue().get(lockKey);
        if (value != null && value.equals(currentValue)) {
            return Boolean.TRUE.equals(redisTemplate.delete(lockKey));
        }
        return false;
    }
}
