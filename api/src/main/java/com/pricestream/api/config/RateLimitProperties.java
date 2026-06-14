package com.pricestream.api.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for the API rate limiter.
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "api.rate-limit")
public class RateLimitProperties {
    
    /**
     * Whether the rate limiter is enabled for the public API.
     */
    private boolean enabled = true;

    /**
     * The maximum number of requests allowed per time window.
     */
    private int requestsPerWindow = 60;

    /**
     * The duration of the time window in seconds.
     */
    private int windowSeconds = 60;
}
