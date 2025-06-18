package com.system.design.ratelimit;

import com.google.common.util.concurrent.RateLimiter;


import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GuavaRateLimiterExample {

    private final Map<String, RateLimiter> ipRateLimiters = new ConcurrentHashMap<>();
    private static final double PERMITS_PER_SECOND = 5.0 / 60.0; // 5 requests per minute

    public boolean isAllowed(String ip) {
        ipRateLimiters.putIfAbsent(ip, RateLimiter.create(PERMITS_PER_SECOND));
        RateLimiter rateLimiter = ipRateLimiters.get(ip);

        return rateLimiter.tryAcquire(); // returns true if allowed, false if rate limited
    }

    public void handleRequest(String ip) {
        if (isAllowed(ip)) {
            System.out.println("200 OK - Request allowed for IP: " + ip);
        } else {
            System.out.println("429 Too Many Requests for IP: " + ip);
        }
    }

    public static void main(String[] args) throws InterruptedException {
        GuavaRateLimiterExample limiter = new GuavaRateLimiterExample();
        String ip = "192.168.1.10";

        for (int i = 1; i <= 10; i++) {
            limiter.handleRequest(ip);
            Thread.sleep(500); // simulate 500 ms between requests
        }
    }
}
