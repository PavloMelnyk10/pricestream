package com.pricestream.core.domain;

import java.math.BigDecimal;
import java.time.Instant;
import lombok.Builder;

/**
 * Core domain representation of a Crypto Price read-model.
 */
@Builder
public record CryptoPrice(
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
