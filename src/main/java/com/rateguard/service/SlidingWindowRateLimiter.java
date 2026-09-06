package com.rateguard.service;

import com.rateguard.dto.RateLimitResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
public class SlidingWindowRateLimiter {

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final int LIMIT = 10;
    private static final int WINDOW_SECONDS = 60;

    public RateLimitResponse checkLimit(String clientId) {
        String key = "sliding_window:" + clientId;
        ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();

        long now = System.currentTimeMillis();
        long windowStart = now - (WINDOW_SECONDS * 1000L);

        zSetOps.removeRangeByScore(key, 0, windowStart);

        Long currentCount = zSetOps.zCard(key);
        long count = currentCount != null ? currentCount : 0;

        if (count >= LIMIT) {
            redisTemplate.expire(key, Duration.ofSeconds(WINDOW_SECONDS));
            return new RateLimitResponse(false, 0, WINDOW_SECONDS);
        }

        String member = now + ":" + UUID.randomUUID();
        zSetOps.add(key, member, now);
        redisTemplate.expire(key, Duration.ofSeconds(WINDOW_SECONDS));

        return new RateLimitResponse(true, LIMIT - (count + 1), 0);
    }
}