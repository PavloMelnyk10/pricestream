package com.pricestream.core.dto;

import com.pricestream.core.port.Publishable;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

/**
 * Standardized crypto market data tick.
 *
 * <p>Symbols follow the {@code BASE/QUOTE} convention (e.g. "BTCUSDT").
 * The {@code source} field identifies which API produced the data, enabling
 * multiple collectors to write to the same Kafka topic without ambiguity.
 */
@Value
@Builder
@Jacksonized
@AllArgsConstructor
public class CryptoMarketTick implements Publishable {

    /** CoinGecko coin ID, e.g. "bitcoin". Null for non-CoinGecko sources. */
    String coinId;

    /** e.g. "BTCUSDT" — base currency + quote currency */
    String symbol;

    /** Human-readable name, e.g. "Bitcoin" */
    String name;

    /** Base currency code, e.g. "BTC" */
    String baseCurrency;

    /** Quote currency code, e.g. "USDT" */
    String quoteCurrency;

    /** Current price in quote currency */
    BigDecimal price;

    /** Market capitalisation */
    BigDecimal marketCap;

    /** CoinGecko market cap rank (1 = largest). Null for non-ranked sources. */
    Integer marketCapRank;

    /** 24-hour trading volume in quote currency */
    BigDecimal volume24h;

    /** 24-hour highest price */
    BigDecimal high24h;

    /** 24-hour lowest price */
    BigDecimal low24h;

    /** Absolute price change over 24 hours */
    BigDecimal priceChange24h;

    /** Percentage price change over 24 hours */
    BigDecimal priceChangePct24h;

    /** Number of coins currently in circulation */
    BigDecimal circulatingSupply;

    /** Total supply (may be null if not available) */
    BigDecimal totalSupply;

    /** Which external API produced this record, e.g. "COINGECKO" */
    String source;

    /** Timestamp when this data was fetched from the external API */
    Instant fetchedAt;

    /** Timestamp reported by the external API for when the data was last updated */
    Instant lastUpdated;

    @Override
    public String partitionKey() {
        return symbol;
    }

    @Override
    public String source() {
        return source;
    }

    @Override
    public Instant fetchedAt() {
        return fetchedAt;
    }
}
