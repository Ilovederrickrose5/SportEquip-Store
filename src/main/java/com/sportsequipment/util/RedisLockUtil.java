package com.sportsequipment.util;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class RedisLockUtil {

    private final RedissonClient redissonClient;

    private static final String LOCK_PREFIX = "lock:";
    private static final long DEFAULT_EXPIRE_TIME = 30L;

    public RedisLockUtil(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    public boolean tryLock(String key, String value) {
        return tryLock(key, value, DEFAULT_EXPIRE_TIME, TimeUnit.SECONDS);
    }

    public boolean tryLock(String key, String value, long expireTime, TimeUnit unit) {
        validateKey(key);
        validateValue(value);
        validateTimeout(expireTime);

        String lockKey = LOCK_PREFIX + key;
        RLock lock = redissonClient.getLock(lockKey);
        try {
            return lock.tryLock(0, expireTime, unit);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    public boolean unlock(String key, String value) {
        validateKey(key);
        validateValue(value);

        String lockKey = LOCK_PREFIX + key;
        RLock lock = redissonClient.getLock(lockKey);
        if (lock.isHeldByCurrentThread()) {
            lock.unlock();
            return true;
        }
        return false;
    }

    public void lock(String key) {
        validateKey(key);
        String lockKey = LOCK_PREFIX + key;
        redissonClient.getLock(lockKey).lock();
    }

    public void lock(String key, long leaseTime, TimeUnit unit) {
        validateKey(key);
        String lockKey = LOCK_PREFIX + key;
        redissonClient.getLock(lockKey).lock(leaseTime, unit);
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
