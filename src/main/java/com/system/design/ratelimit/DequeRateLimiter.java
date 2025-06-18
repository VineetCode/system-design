package com.system.design.ratelimit;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class DequeRateLimiter {

     // Max 5 requests per 60 seconds
     private static final int MAX_REQUESTS = 5;
     private static final long WINDOW_SIZE = 60 * 1000; //in ms

     // Store: IP -> list of request timestamps
     private final Map<String, Deque<Long>> requestLogs = new ConcurrentHashMap<>();


     public boolean isAllowed(String ip) {
        long now = System.currentTimeMillis();
        requestLogs.putIfAbsent(ip, new ArrayDeque<>());
        Deque<Long> timestamps = requestLogs.get(ip);

        synchronized (timestamps) {
            // Remove timestamps older than 1 min
            while (!timestamps.isEmpty() && now - timestamps.peekFirst() > WINDOW_SIZE) {
                timestamps.pollFirst();
            }

            if (timestamps.size() < MAX_REQUESTS) {
                // Allow request
                timestamps.offerLast(now);
                return true;
            } else{
               // Block request: too many requests
                return false;
            }
        }
     }

    // Simulate API request
    public void handleRequest(String ip) {
        if (isAllowed(ip)) {
            System.out.println("200 OK - Request allowed for IP: " + ip);
        } else {
            System.out.println("429 Too Many Requests for IP: " + ip);
        }
    }

    // Example usage
    public static void main(String[] args) throws InterruptedException {
        DequeRateLimiter rateLimiter = new DequeRateLimiter();

        String ip = "192.168.1.10";

        // Simulate 7 requests from same IP
        for (int i = 1; i <= 7; i++) {
            rateLimiter.handleRequest(ip);
            Thread.sleep(500); // simulate small delay between requests
        }
    }
}
