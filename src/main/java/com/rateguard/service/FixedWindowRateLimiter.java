package com.rateguard.service;

import com.rateguard.dto.RateLimitResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class FixedWindowRateLimiter {

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final int LIMIT = 10;
    private static final int WINDOW_SECONDS = 60;

    public RateLimitResponse checkLimit(String clientId) {
        String key = "fixed_window:" + clientId;

        Long count = redisTemplate.opsForValue().increment(key);

        if (count != null && count == 1) {
            redisTemplate.expire(key, Duration.ofSeconds(WINDOW_SECONDS));
        }

        Long ttl = redisTemplate.getExpire(key);
        long retryAfter = (ttl != null && ttl > 0) ? ttl : WINDOW_SECONDS;

        if (count != null && count > LIMIT) {
            return new RateLimitResponse(false, 0, retryAfter);
        }

        long remaining = LIMIT - (count != null ? count : 0);
        return new RateLimitResponse(true, remaining, retryAfter);
    }
}