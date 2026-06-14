package com.pricestream.collector.config;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Configuration properties for the CoinGecko API client.
 */
@Validated
@ConfigurationProperties(prefix = "coingecko")
public record CoinGeckoProperties(

        @NotBlank
        String baseUrl,

        /** Optional API key for CoinGecko. */
        String apiKey,

        /** Target quote currency for price data. */
        @NotBlank
        String vsCurrency,

        /** Number of items to retrieve per page. */
        @Min(1) @Max(250)
        int perPage,

        /** Maximum number of pages to retrieve per fetch cycle. */
        @Min(1)
        int maxPages
) {
}
