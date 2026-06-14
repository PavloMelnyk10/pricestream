package com.pricestream.collector.adapter.out.coingecko;

import com.pricestream.core.dto.CryptoMarketTick;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Mapper that converts CoinGecko market items into the standard crypto market tick format.
 */
@Component
public class CoinGeckoTickMapper {

    public static final String SOURCE = "COINGECKO";

    public CryptoMarketTick adapt(CoinGeckoMarketItem item, String quoteCurrency, Instant fetchedAt) {
        if (item == null) {
            return null;
        }

        return CryptoMarketTick.builder()
                .coinId(item.id())
                .symbol((item.symbol() + quoteCurrency).toUpperCase())
                .name(item.name())
                .baseCurrency(item.symbol().toUpperCase())
                .quoteCurrency(quoteCurrency.toUpperCase())
                .price(item.currentPrice())
                .marketCap(item.marketCap())
                .marketCapRank(item.marketCapRank())
                .volume24h(item.totalVolume())
                .high24h(item.high24h())
                .low24h(item.low24h())
                .priceChange24h(item.priceChange24h())
                .priceChangePct24h(item.priceChangePct24h())
                .circulatingSupply(item.circulatingSupply())
                .totalSupply(item.totalSupply())
                .source(SOURCE)
                .fetchedAt(fetchedAt)
                .lastUpdated(item.lastUpdated())
                .build();
    }
}
