package com.pricestream.api.cache;

import com.pricestream.core.domain.AssetType;
import com.pricestream.core.domain.MarketDataUpdatedEvent;
import jakarta.annotation.PreDestroy;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Event listener that evicts relevant caches when market data is updated.
 */
@Slf4j
@Component
@AllArgsConstructor
public class MarketDataCacheListener {

    /** Map of cache names grouped by asset type. */
    private static final Map<AssetType, List<String>> CACHE_NAMES_BY_TYPE;

    static {
        CACHE_NAMES_BY_TYPE = new EnumMap<>(AssetType.class);
        CACHE_NAMES_BY_TYPE.put(AssetType.CRYPTO, List.of(
                "cryptoPrices", "latestCryptoPrice", "cryptoPriceHistory", "cryptoSymbols"));
        CACHE_NAMES_BY_TYPE.put(AssetType.FX, List.of(
                "fxPrices", "latestFxPrice"));
        CACHE_NAMES_BY_TYPE.put(AssetType.STOCK, List.of(
                "stockPrices", "latestStockPrice"));
    }

    private final CacheManager cacheManager;
    private final Map<AssetType, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    private static final long DEBOUNCE_DELAY_SECONDS = 2;

    @EventListener
    public void onMarketDataUpdated(MarketDataUpdatedEvent event) {
        AssetType assetType = event.assetType();

        scheduledTasks.compute(assetType, (_, existingTask) -> {
            if (existingTask != null && !existingTask.isDone()) {
                existingTask.cancel(false);
            }

            return scheduler.schedule(() -> {
                List<String> cacheNames = CACHE_NAMES_BY_TYPE.getOrDefault(assetType, List.of());
                log.debug("Evicting {} caches for asset type: {}", cacheNames.size(), assetType);
                cacheNames.forEach(this::evictCache);
            }, DEBOUNCE_DELAY_SECONDS, TimeUnit.SECONDS);
        });
    }

    private void evictCache(String cacheName) {
        var cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.clear();
            log.debug("Cleared cache: {}", cacheName);
        }
    }

    @PreDestroy
    public void destroy() {
        if (!scheduler.isShutdown()) {
            scheduler.shutdownNow();
        }
    }
}
