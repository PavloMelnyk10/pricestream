package com.pricestream.collector.adapter.out.coingecko;

import com.pricestream.collector.config.CoinGeckoProperties;
import com.pricestream.collector.config.CollectorProperties;
import com.pricestream.core.domain.AssetType;
import com.pricestream.core.dto.CryptoMarketTick;
import com.pricestream.core.port.out.MarketDataSource;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Market data source implementation for fetching cryptocurrency prices from CoinGecko.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CoinGeckoCryptoSource implements MarketDataSource<CryptoMarketTick> {

    private final CoinGeckoClient client;
    private final CoinGeckoTickMapper adapter;
    private final CoinGeckoProperties props;
    private final CollectorProperties collectorProps;

    @Override
    @CircuitBreaker(name = "coingecko", fallbackMethod = "fetchFallback")
    public List<CryptoMarketTick> fetch() {
        Instant fetchedAt = Instant.now();
        List<CryptoMarketTick> result = new ArrayList<>();

        for (int page = 1; page <= props.maxPages(); page++) {
            List<CoinGeckoMarketItem> items = client.fetchMarketsPage(page);



            items.stream()
                    .map(item -> adapter.adapt(item, props.vsCurrency(), fetchedAt))
                    .forEach(result::add);

            log.debug("CoinGecko: page {} yielded {} items (total so far: {})",
                    page, items.size(), result.size());

            if (items.size() < props.perPage()) {
                break;
            }
        }

        log.info("CoinGecko fetch complete: {} {} ticks fetched", result.size(), props.vsCurrency().toUpperCase());
        return result;
    }

    /**
     * Fallback method invoked by the circuit breaker when fetch fails.
     */
    @SuppressWarnings("unused")
    public List<CryptoMarketTick> fetchFallback(Exception ex) {
        log.warn("CoinGecko circuit breaker / fallback triggered: {}", ex.getMessage());
        return Collections.emptyList();
    }

    @Override
    public AssetType assetType() {
        return AssetType.CRYPTO;
    }

    @Override
    public String sourceName() {
        return "COINGECKO";
    }

    @Override
    public String topicName() {
        return collectorProps.kafka().topicCrypto();
    }

    @Override
    public long delayMs() {
        return collectorProps.schedule().cryptoDelayMs();
    }

    @Override
    public long initialDelayMs() {
        return collectorProps.schedule().cryptoInitialDelayMs();
    }
}
