package com.pricestream.collector.adapter.out.coingecko;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Data record representing a raw market item from the CoinGecko API response.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record CoinGeckoMarketItem(

        String id,

        String symbol,

        String name,

        @JsonProperty("current_price")
        BigDecimal currentPrice,

        @JsonProperty("market_cap")
        BigDecimal marketCap,

        @JsonProperty("market_cap_rank")
        Integer marketCapRank,

        @JsonProperty("total_volume")
        BigDecimal totalVolume,

        @JsonProperty("high_24h")
        BigDecimal high24h,

        @JsonProperty("low_24h")
        BigDecimal low24h,

        @JsonProperty("price_change_24h")
        BigDecimal priceChange24h,

        @JsonProperty("price_change_percentage_24h")
        BigDecimal priceChangePct24h,

        @JsonProperty("circulating_supply")
        BigDecimal circulatingSupply,

        @JsonProperty("total_supply")
        BigDecimal totalSupply,

        @JsonProperty("last_updated")
        Instant lastUpdated
) {
}
