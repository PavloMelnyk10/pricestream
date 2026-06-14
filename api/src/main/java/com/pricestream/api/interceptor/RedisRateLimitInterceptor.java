package com.pricestream.api.interceptor;

import com.pricestream.api.config.RateLimitProperties;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.List;

/**
 * Web interceptor that enforces rate limits per IP address using Redis.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisRateLimitInterceptor implements HandlerInterceptor {

    private final StringRedisTemplate redisTemplate;
    private final RateLimitProperties properties;

    private DefaultRedisScript<Long> redisScript;

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request,
                             @NonNull HttpServletResponse response,
                             @NonNull Object handler) throws Exception {
        if (!properties.isEnabled()) {
            return true;
        }

        String clientIp = getClientIp(request);
        String key = "rate_limit:ip:" + clientIp;
        int limit = properties.getRequestsPerWindow();
        int window = properties.getWindowSeconds();

        Long result = redisTemplate.execute(
                getRedisScript(),
                List.of(key),
                String.valueOf(limit),
                String.valueOf(window)
        );

        boolean allowed = result != null && result == 1L;

        response.setHeader("X-RateLimit-Limit", String.valueOf(limit));
        response.setHeader("X-RateLimit-Window-Seconds", String.valueOf(window));

        if (!allowed) {
            log.warn("Rate limit exceeded for IP: {}", clientIp);
            response.setStatus(429);
            response.setHeader("Retry-After", String.valueOf(window));
            response.getWriter().write("Too Many Requests. Rate limit of " + limit + " requests per " + window + " seconds exceeded.");
            return false;
        }

        return true;
    }

    private DefaultRedisScript<Long> getRedisScript() {
        if (this.redisScript == null) {
            DefaultRedisScript<Long> script = new DefaultRedisScript<>();
            script.setLocation(new ClassPathResource("scripts/rate-limiter.lua"));
            script.setResultType(Long.class);
            this.redisScript = script;
        }
        return this.redisScript;
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isEmpty()) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0].trim();
    }
}
