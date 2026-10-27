package com.priceservice.app.infrastructure.adapter.in.web.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Per-client token-bucket rate limiter.
 * <p>
 * Replaces the previous implementation that spawned a new {@link Thread} on
 * every request (a resource leak and not a real sliding window). Here each
 * client IP gets a bucket that refills lazily based on elapsed time, so there
 * are no background threads and the limit is enforced over a real time window.
 */
public class RateLimitFilter implements Filter {

    private final long capacity;
    private final double refillTokensPerNano;
    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();

    public RateLimitFilter(long capacity, long refillTokens, long refillPeriodSeconds) {
        this.capacity = capacity;
        long periodNanos = refillPeriodSeconds * 1_000_000_000L;
        this.refillTokensPerNano = (double) refillTokens / periodNanos;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        String clientIp = request.getRemoteAddr();
        Bucket bucket = buckets.computeIfAbsent(clientIp, k -> new Bucket(capacity));

        if (bucket.tryConsume(refillTokensPerNano, capacity)) {
            chain.doFilter(request, response);
        } else {
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            httpResponse.setStatus(429); // 429 Too Many Requests
            httpResponse.getWriter().write("Too many requests");
        }
    }

    /** Lazily-refilled token bucket. Access is guarded by intrinsic lock. */
    private static final class Bucket {
        private double tokens;
        private long lastRefillNanos;

        Bucket(long initialTokens) {
            this.tokens = initialTokens;
            this.lastRefillNanos = System.nanoTime();
        }

        synchronized boolean tryConsume(double refillPerNano, long capacity) {
            long now = System.nanoTime();
            tokens = Math.min(capacity, tokens + (now - lastRefillNanos) * refillPerNano);
            lastRefillNanos = now;
            if (tokens >= 1.0) {
                tokens -= 1.0;
                return true;
            }
            return false;
        }
    }
}
