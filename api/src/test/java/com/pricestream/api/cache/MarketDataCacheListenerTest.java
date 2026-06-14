package com.pricestream.api.cache;

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.awaitility.Awaitility.await;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

import com.pricestream.core.domain.AssetType;
import com.pricestream.core.domain.MarketDataUpdatedEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

@ExtendWith(MockitoExtension.class)
@DisplayName("Market Data Cache Listener Tests")
class MarketDataCacheListenerTest {

    @Mock
    private CacheManager cacheManager;

    @Mock
    private Cache cache;

    @InjectMocks
    private MarketDataCacheListener listener;

    @Test
    @DisplayName("Should evict caches after debounce delay")
    void shouldEvictCaches() {
        // Given
        lenient().when(cacheManager.getCache("cryptoPrices")).thenReturn(cache);
        lenient().when(cacheManager.getCache("latestCryptoPrice")).thenReturn(cache);
        lenient().when(cacheManager.getCache("cryptoPriceHistory")).thenReturn(cache);
        lenient().when(cacheManager.getCache("cryptoSymbols")).thenReturn(cache);

        MarketDataUpdatedEvent event = new MarketDataUpdatedEvent(AssetType.CRYPTO);

        // When
        listener.onMarketDataUpdated(event);

        // Then
        // Wait longer than the 2 seconds debounce delay
        await().atMost(3000, MILLISECONDS).untilAsserted(() -> {
            verify(cacheManager, atLeastOnce()).getCache("cryptoPrices");
            verify(cache, atLeastOnce()).clear();
        });
    }
    
    @Test
    @DisplayName("Should cancel and debounce multiple rapid events")
    void shouldDebounceEvents() {
        // Given
        lenient().when(cacheManager.getCache("cryptoPrices")).thenReturn(cache);

        MarketDataUpdatedEvent event = new MarketDataUpdatedEvent(AssetType.CRYPTO);

        // When
        listener.onMarketDataUpdated(event);
        await().pollDelay(1000, MILLISECONDS).until(() -> true);
        listener.onMarketDataUpdated(event); // Reset the timer

        // At this point, the first task should be cancelled, and second is counting down 2s.
        // Wait another 1.2s - total time since first event is 2.2s, but since we reset, it shouldn't trigger yet.
        await().pollDelay(1200, MILLISECONDS).until(() -> true);
        verifyNoInteractions(cacheManager); // Still hasn't triggered
        
        // Wait another 1s to exceed the 2nd timer.
        await().atMost(2000, MILLISECONDS).untilAsserted(()
                -> verify(cacheManager, atLeastOnce()).getCache("cryptoPrices"));
    }
}
