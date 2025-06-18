package com.system.design.ratelimit;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TokenBucketRateLimiter {

    private static final int MAX_TOKENS = 5;
    private static final long REFILL_INTERVAL_MS = 12_000; // refill 1 token every 12 seconds


    static class Bucket {
        double tokens;
        long lastRefillTimestamps;

        Bucket() {
            this.tokens = MAX_TOKENS;
            this.lastRefillTimestamps = System.currentTimeMillis();
        }
    }

    private final Map<String, Bucket> ipBuckets = new ConcurrentHashMap<>();

    public boolean isAllowed(String ip) {
        long now = System.currentTimeMillis();
        ipBuckets.putIfAbsent(ip, new Bucket());
        Bucket bucket = ipBuckets.get(ip);

        synchronized (bucket) {
            // Refill tokens
            long timeSinceLast = now - bucket.lastRefillTimestamps;
            double tokensToAdd = (timeSinceLast / (double) REFILL_INTERVAL_MS);

            System.out.println("timeSinceLast: "+timeSinceLast+ " tokensToAdd: "+tokensToAdd);

            if (tokensToAdd > 0) {
                bucket.tokens = Math.min(MAX_TOKENS, bucket.tokens + tokensToAdd);
                bucket.lastRefillTimestamps = now;
            }

            if (bucket.tokens >= 1) {
                bucket.tokens -= 1;
                System.out.println("Token: "+bucket.tokens);
                return true;
            } else {
                return false;
            }
        }
    }

    public void handleRequest(String ip) {
        if (isAllowed(ip)) {
            System.out.println("200 OK - Request allowed for IP: " + ip);
        } else {
            System.out.println("429 Too Many Requests for IP: " + ip);
        }
    }

    public static void main(String[] args) throws InterruptedException {
        TokenBucketRateLimiter rateLimiter = new TokenBucketRateLimiter();
        String ip = "192.168.1.10";

        for (int i = 1; i <= 10; i++) {
            rateLimiter.handleRequest(ip);
            Thread.sleep(2000); // simulate 2 sec gap between requests
        }
    }
}
