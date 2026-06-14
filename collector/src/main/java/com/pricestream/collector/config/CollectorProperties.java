package com.pricestream.collector.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Configuration properties for collector scheduling and Kafka settings.
 */
@Validated
@ConfigurationProperties(prefix = "collector")
public record CollectorProperties(

        Schedule schedule,
        Kafka kafka
) {
    public record Schedule(

            /** Delay between fetch cycles in milliseconds. */
            long cryptoDelayMs,

            /** Initial delay before the first fetch cycle in milliseconds. */
            long cryptoInitialDelayMs
    ) {
    }

    public record Kafka(

            @NotBlank
            String topicCrypto
    ) {
    }
}
