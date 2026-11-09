package com.aquaai.shared.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * IP-based sliding window rate limiter filter for sensitive authentication endpoints.
 * Protects login, registration, password reset, and token refresh against brute force.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitingFilter extends OncePerRequestFilter {

    private final RateLimiterProperties properties;
    private final ObjectMapper objectMapper;

    private final Map<String, RequestCounter> requestCounts = new ConcurrentHashMap<>();
    private volatile long lastCleanupTimestamp = System.currentTimeMillis();

    private static class RequestCounter {
        final long windowStartEpochMinute;
        final AtomicInteger count = new AtomicInteger(0);

        RequestCounter(long windowStartEpochMinute) {
            this.windowStartEpochMinute = windowStartEpochMinute;
        }
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        if (!properties.isEnabled()) {
            filterChain.doFilter(request, response);
            return;
        }

        String path = request.getRequestURI();
        if (isRateLimitedPath(path)) {
            String clientIp = resolveClientIp(request);
            int maxAllowed = getLimitForPath(path);

            if (isRateLimitExceeded(clientIp, path, maxAllowed)) {
                log.warn("Rate limit exceeded for IP: {} on endpoint: {}", clientIp, path);
                rejectWithTooManyRequests(response);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean isRateLimitedPath(String path) {
        return path.startsWith("/api/v1/auth/login") ||
               path.startsWith("/api/v1/auth/register") ||
               path.startsWith("/api/v1/auth/refresh") ||
               path.startsWith("/api/v1/auth/password-reset");
    }

    private int getLimitForPath(String path) {
        if (path.startsWith("/api/v1/auth/login")) {
            return properties.getLoginLimitPerMinute();
        } else if (path.startsWith("/api/v1/auth/refresh")) {
            return properties.getRefreshLimitPerMinute();
        } else {
            return properties.getDefaultAuthLimitPerMinute();
        }
    }

    private boolean isRateLimitExceeded(String clientIp, String path, int maxAllowed) {
        long currentMinute = System.currentTimeMillis() / 60000;
        String key = clientIp + ":" + path + ":" + currentMinute;

        RequestCounter counter = requestCounts.computeIfAbsent(key, k -> new RequestCounter(currentMinute));
        int currentCount = counter.count.incrementAndGet();

        cleanupExpiredEntriesIfNeeded(currentMinute);

        return currentCount > maxAllowed;
    }

    private void cleanupExpiredEntriesIfNeeded(long currentMinute) {
        long now = System.currentTimeMillis();
        // Run cleanup at most once per 2 minutes
        if (now - lastCleanupTimestamp > 120_000) {
            lastCleanupTimestamp = now;
            requestCounts.entrySet().removeIf(entry -> entry.getValue().windowStartEpochMinute < currentMinute - 2);
        }
    }

    private String resolveClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(xForwardedFor)) {
            String firstIp = xForwardedFor.split(",")[0].trim();
            if (StringUtils.hasText(firstIp)) {
                return firstIp;
            }
        }
        return request.getRemoteAddr();
    }

    private void rejectWithTooManyRequests(HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setHeader("Retry-After", "60");

        Map<String, Object> errorBody = Map.of(
                "success", false,
                "error", Map.of(
                        "code", "RATE_LIMIT_EXCEEDED",
                        "message", "Too many requests. Please try again after 60 seconds."
                ),
                "timestamp", Instant.now().toString()
        );

        objectMapper.writeValue(response.getOutputStream(), errorBody);
    }
}
