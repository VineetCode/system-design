package com.system.design.Interceptor;

import com.system.design.ratelimit.SlidingWindowRateLimiter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class RateLimitingInterceptor implements HandlerInterceptor {

    private final SlidingWindowRateLimiter rateLimiter = new SlidingWindowRateLimiter();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String clientIp = getClientIP(request);
        System.out.println("ClientIp: "+clientIp);

        if (!rateLimiter.isAllowed(clientIp)) {
            response.setStatus(429);
            response.getWriter().write("429 Too Many Requests");
            return false; // block request
        }

        return true; // allow request
    }

    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        System.out.println("xfHeader :"+xfHeader);
        if (xfHeader == null) {
            System.out.println("RemoteAddr: "+request.getRemoteAddr());
            return request.getRemoteAddr();
        }
        System.out.println("In case of multiple IPs "+ xfHeader.split(",")[0]);
        return xfHeader.split(",")[0]; // in case of multiple IPs
    }
}
