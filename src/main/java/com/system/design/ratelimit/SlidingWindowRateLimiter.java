package com.system.design.ratelimit;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SlidingWindowRateLimiter {

    // Max 5 requests per 60 seconds
    private static final int MAX_REQUESTS = 5;
    private static final int WINDOW_SIZE = 60 * 1000; //in ms

    static class Counter {
        int count;
        long windowStart;

        Counter() {
            count = 0;
            windowStart = System.currentTimeMillis();
        }
    }

    private final Map<String, Counter> ipRequestCounters = new ConcurrentHashMap<>();

    public boolean isAllowed(String ip) {

        long now = System.currentTimeMillis();
        ipRequestCounters.putIfAbsent(ip, new Counter());
        Counter counter = ipRequestCounters.get(ip);

        synchronized (counter) {

            if (now - counter.windowStart > WINDOW_SIZE) {
               // Window expired — reset counter
                counter.count = 1;
                counter.windowStart = now;
                return true;
            } else {
                if (counter.count < MAX_REQUESTS) {
                   counter.count++;
                   return true;
                } else {
                    return  false;
                }
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
        SlidingWindowRateLimiter rateLimiter = new SlidingWindowRateLimiter();
        String ip = "192.168.1.10";

        for (int i = 1; i <= 7; i++) {
            rateLimiter.handleRequest(ip);
            Thread.sleep(500); // simulate small delay
        }
    }

}
