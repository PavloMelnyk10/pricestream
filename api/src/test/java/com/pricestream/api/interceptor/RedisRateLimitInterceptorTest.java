package com.pricestream.api.interceptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.pricestream.api.config.RateLimitProperties;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

@ExtendWith(MockitoExtension.class)
@DisplayName("Redis Rate Limit Interceptor Tests")
class RedisRateLimitInterceptorTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private RateLimitProperties properties;

    @InjectMocks
    private RedisRateLimitInterceptor interceptor;

    private HttpServletRequest request;
    private HttpServletResponse response;

    @BeforeEach
    void setUp() {
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
    }

    @Test
    @DisplayName("Should allow request when rate limit is disabled")
    void shouldAllowWhenDisabled() throws Exception {
        // Given
        when(properties.isEnabled()).thenReturn(false);

        // When
        boolean result = interceptor.preHandle(request, response, new Object());

        // Then
        assertThat(result).isTrue();
        verifyNoInteractions(redisTemplate);
    }

    @Test
    @DisplayName("Should allow request when rate limit is not exceeded")
    @SuppressWarnings("unchecked")
    void shouldAllowWhenNotExceeded() throws Exception {
        // Given
        when(properties.isEnabled()).thenReturn(true);
        when(properties.getRequestsPerWindow()).thenReturn(10);
        when(properties.getWindowSeconds()).thenReturn(60);
        when(request.getHeader("X-Forwarded-For")).thenReturn("192.168.1.1");
        
        when(redisTemplate.execute(
                any(DefaultRedisScript.class),
                eq(List.of("rate_limit:ip:192.168.1.1")),
                eq("10"),
                eq("60")
        )).thenReturn(1L); // 1L means allowed

        // When
        boolean result = interceptor.preHandle(request, response, new Object());

        // Then
        assertThat(result).isTrue();
        verify(response).setHeader("X-RateLimit-Limit", "10");
        verify(response).setHeader("X-RateLimit-Window-Seconds", "60");
    }

    @Test
    @DisplayName("Should block request when rate limit is exceeded")
    @SuppressWarnings("unchecked")
    void shouldBlockWhenExceeded() throws Exception {
        // Given
        when(properties.isEnabled()).thenReturn(true);
        when(properties.getRequestsPerWindow()).thenReturn(10);
        when(properties.getWindowSeconds()).thenReturn(60);
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("10.0.0.1");
        
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(printWriter);
        
        when(redisTemplate.execute(
                any(DefaultRedisScript.class),
                eq(List.of("rate_limit:ip:10.0.0.1")),
                eq("10"),
                eq("60")
        )).thenReturn(0L); // 0L means blocked

        // When
        boolean result = interceptor.preHandle(request, response, new Object());

        // Then
        assertThat(result).isFalse();
        verify(response).setStatus(429);
        verify(response).setHeader("Retry-After", "60");
        assertThat(stringWriter.toString()).contains("Too Many Requests");
    }
}
