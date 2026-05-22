package com.interview.ordersystem.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.interview.ordersystem.common.ApiResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class RateLimitFilter extends OncePerRequestFilter {
    private final Map<String, ClientBucket> buckets = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${app.rate-limit.requests-per-minute}")
    private int requestsPerMinute;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String clientIp = request.getRemoteAddr();
        ClientBucket bucket = buckets.computeIfAbsent(clientIp, ip -> new ClientBucket());

        if (!bucket.tryConsume(requestsPerMinute)) {
            response.setStatus(429);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write(objectMapper.writeValueAsString(
                    ApiResponse.fail("Too many requests. Try again after a minute.", null)));
            return;
        }

        filterChain.doFilter(request, response);
    }

    private static class ClientBucket {
        private Instant windowStart = Instant.now();
        private final AtomicInteger counter = new AtomicInteger(0);

        synchronized boolean tryConsume(int limit) {
            if (Instant.now().isAfter(windowStart.plusSeconds(60))) {
                windowStart = Instant.now();
                counter.set(0);
            }
            return counter.incrementAndGet() <= limit;
        }
    }
}
