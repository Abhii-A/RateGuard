package com.rateguard.dto;

public class RateLimitResponse {

    private boolean allowed;
    private long remaining;
    private long retryAfterSeconds;

    public RateLimitResponse() {}

    public RateLimitResponse(boolean allowed, long remaining, long retryAfterSeconds) {
        this.allowed = allowed;
        this.remaining = remaining;
        this.retryAfterSeconds = retryAfterSeconds;
    }

    public boolean isAllowed() { return allowed; }
    public void setAllowed(boolean allowed) { this.allowed = allowed; }

    public long getRemaining() { return remaining; }
    public void setRemaining(long remaining) { this.remaining = remaining; }

    public long getRetryAfterSeconds() { return retryAfterSeconds; }
    public void setRetryAfterSeconds(long retryAfterSeconds) { this.retryAfterSeconds = retryAfterSeconds; }
}