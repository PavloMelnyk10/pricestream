package com.pricestream.api.dto;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * API response object representing a cryptocurrency price record.
 */
@Builder
public record CryptoPriceResponse(
        String symbol,
        String name,
        String baseAsset,
        String quoteAsset,
        BigDecimal price,
        BigDecimal marketCap,
        BigDecimal volume24h,
        BigDecimal change24h,
        BigDecimal changePct24h,
        BigDecimal circulatingSupply,
        BigDecimal totalSupply,
        String source,
        Instant fetchedAt,
        Instant timestamp
) {
}
