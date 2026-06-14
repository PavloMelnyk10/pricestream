package com.pricestream.api.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.pricestream.core.port.out.CryptoPriceReadPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.StringRedisTemplate;

@DisplayName("API Config Tests")
class ApiConfigTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(
                    ApiConfig.class, 
                    CacheConfig.class, 
                    WebMvcConfig.class, 
                    RateLimitProperties.class,
                    com.pricestream.api.interceptor.RedisRateLimitInterceptor.class
            )
            .withBean(StringRedisTemplate.class, () -> org.mockito.Mockito.mock(org.springframework.data.redis.core.StringRedisTemplate.class))
            .withBean(CryptoPriceReadPort.class, () -> org.mockito.Mockito.mock(com.pricestream.core.port.out.CryptoPriceReadPort.class))
            .withBean(CacheManager.class, () -> org.mockito.Mockito.mock(org.springframework.cache.CacheManager.class))
            .withPropertyValues(
                    "api.rate-limit.enabled=true",
                    "api.rate-limit.requests-per-window=10",
                    "api.rate-limit.window-seconds=60"
            );

    @Test
    @DisplayName("Should load all API configurations successfully")
    void shouldLoadConfigs() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(CacheManager.class);
            assertThat(context).hasSingleBean(RateLimitProperties.class);
        });
    }
}
