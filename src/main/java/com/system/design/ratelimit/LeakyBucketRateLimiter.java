package com.system.design.ratelimit;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LeakyBucketRateLimiter {

    private static final int BUCKET_CAPACITY = 5;
    private static final long LEAK_INTERVAL_MS = 12_000; // leak 1 token every 12 seconds

    static class Bucket {
        int currentFill;
        long lastLeakTimestamp;

        Bucket() {
            this.currentFill = 0;
            this.lastLeakTimestamp = System.currentTimeMillis();
        }
    }

    private final Map<String, Bucket> ipBuckets = new ConcurrentHashMap<>();

    public boolean isAllowed(String ip) {
        long now = System.currentTimeMillis();

        ipBuckets.putIfAbsent(ip, new Bucket());
        Bucket bucket = ipBuckets.get(ip);

        synchronized (bucket) {
            // Calculate how many tokens have leaked
            long timeSinceLastLeak = now - bucket.lastLeakTimestamp;
            int leakedTokens = (int)(timeSinceLastLeak / LEAK_INTERVAL_MS);

            if (leakedTokens > 0) {
                bucket.currentFill = Math.max(0, bucket.currentFill - leakedTokens);
                bucket.lastLeakTimestamp += leakedTokens * LEAK_INTERVAL_MS;
            }

            if (bucket.currentFill < BUCKET_CAPACITY) {
                bucket.currentFill += 1; // Add new token (new request)
                return true;
            } else {
                return false; // Bucket full → reject
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
        LeakyBucketRateLimiter rateLimiter = new LeakyBucketRateLimiter();
        String ip = "192.168.1.10";

        for (int i = 1; i <= 10; i++) {
            rateLimiter.handleRequest(ip);
            Thread.sleep(2000); // simulate 2 sec gap between requests
        }
    }
}

