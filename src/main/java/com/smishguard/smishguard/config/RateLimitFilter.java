package com.smishguard.smishguard.config;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Simple per-IP rate limiter using a sliding window of 60 seconds.
 * Default: 30 requests per minute per IP.
 * Responds with 429 Too Many Requests if exceeded.
 */
@Component
@Order(1)
public class RateLimitFilter implements Filter {

    @Value("${app.rate-limit:30}")
    private int maxRequests;

    // Map of IP → (window start millis, request count)
    private final Map<String, long[]> counters = new ConcurrentHashMap<>();

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest  httpReq = (HttpServletRequest)  req;
        HttpServletResponse httpRes = (HttpServletResponse) res;

        // Only rate-limit API endpoints
        if (!httpReq.getRequestURI().startsWith("/api/")) {
            chain.doFilter(req, res);
            return;
        }

        String ip = getClientIp(httpReq);
        long now = System.currentTimeMillis();

        long[] entry = counters.computeIfAbsent(ip, k -> new long[]{now, 0});
        synchronized (entry) {
            // Reset window if 60 seconds have passed
            if (now - entry[0] > 60_000) {
                entry[0] = now;
                entry[1] = 0;
            }
            entry[1]++;
            if (entry[1] > maxRequests) {
                httpRes.setStatus(429);
                httpRes.setContentType("application/json");
                httpRes.getWriter().write(
                    "{\"error\":\"Too many requests. Try again in a minute.\"}");
                return;
            }
        }
        chain.doFilter(req, res);
    }

    private String getClientIp(HttpServletRequest req) {
        String forwarded = req.getHeader("X-Forwarded-For");
        return (forwarded != null && !forwarded.isBlank())
            ? forwarded.split(",")[0].trim()
            : req.getRemoteAddr();
    }
}
