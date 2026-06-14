package com.pricestream.processor.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * MongoDB document representing a cryptocurrency price record.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "crypto_prices")
@CompoundIndex(name = "symbol_timestamp_idx", def = "{'symbol': 1, 'timestamp': 1}", unique = true)
public class CryptoPriceDocument {

    @Id
    private String id;

    private String symbol;
    private String name;
    private String baseAsset;
    private String quoteAsset;
    private BigDecimal price;
    private BigDecimal marketCap;
    private BigDecimal volume24h;
    private BigDecimal change24h;
    private BigDecimal changePct24h;
    private BigDecimal circulatingSupply;
    private BigDecimal totalSupply;
    private String source;
    private Instant fetchedAt;
    private Instant timestamp;
}
