package com.rateguard.service;

import com.rateguard.dto.RateLimitResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Service
public class TokenBucketRateLimiter {

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final int CAPACITY = 10;
    private static final double REFILL_RATE = 1.0;

    public synchronized RateLimitResponse checkLimit(String clientId) {
        String key = "token_bucket:" + clientId;
        HashOperations<String, String, String> hashOps = redisTemplate.opsForHash();

        Map<String, String> bucket = hashOps.entries(key);
        long now = System.currentTimeMillis();

        double tokens;
        long lastRefill;

        if (bucket.isEmpty()) {
            tokens = CAPACITY;
            lastRefill = now;
        } else {
            tokens = Double.parseDouble(bucket.get("tokens"));
            lastRefill = Long.parseLong(bucket.get("lastRefill"));
        }

        double elapsedSeconds = (now - lastRefill) / 1000.0;
        double refilled = Math.min(CAPACITY, tokens + elapsedSeconds * REFILL_RATE);

        boolean allowed;
        if (refilled >= 1) {
            refilled -= 1;
            allowed = true;
        } else {
            allowed = false;
        }

        Map<String, String> updated = new HashMap<>();
        updated.put("tokens", String.valueOf(refilled));
        updated.put("lastRefill", String.valueOf(now));
        hashOps.putAll(key, updated);
        redisTemplate.expire(key, Duration.ofMinutes(10));

        long retryAfter = allowed ? 0 : (long) Math.ceil((1 - refilled) / REFILL_RATE);
        return new RateLimitResponse(allowed, (long) refilled, retryAfter);
    }
}