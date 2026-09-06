package com.rateguard.controller;

import com.rateguard.dto.RateLimitResponse;
import com.rateguard.service.FixedWindowRateLimiter;
import com.rateguard.service.SlidingWindowRateLimiter;
import com.rateguard.service.TokenBucketRateLimiter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rate-limit")
public class RateLimitController {

    @Autowired private FixedWindowRateLimiter fixedWindowRateLimiter;

    @PostMapping("/fixed-window/check")
    public ResponseEntity<RateLimitResponse> checkFixedWindow(@RequestParam String clientId) {
        return respond(fixedWindowRateLimiter.checkLimit(clientId));
    }

    private ResponseEntity<RateLimitResponse> respond(RateLimitResponse result) {
        if (result.isAllowed()) {
            return ResponseEntity.ok(result);
        }
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(result);
    }

    @Autowired private TokenBucketRateLimiter tokenBucketRateLimiter;

    @PostMapping("/token-bucket/check")
    public ResponseEntity<RateLimitResponse> checkTokenBucket(@RequestParam String clientId) {
        return respond(tokenBucketRateLimiter.checkLimit(clientId));
    }

    @Autowired private SlidingWindowRateLimiter slidingWindowRateLimiter;

    @PostMapping("/sliding-window/check")
    public ResponseEntity<RateLimitResponse> checkSlidingWindow(@RequestParam String clientId) {
        return respond(slidingWindowRateLimiter.checkLimit(clientId));
    }
}