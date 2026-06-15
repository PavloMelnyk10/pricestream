package com.pricestream.collector.adapter.out.coingecko;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pricestream.collector.config.CoinGeckoProperties;
import com.pricestream.collector.config.CollectorProperties;
import com.pricestream.core.domain.AssetType;
import com.pricestream.core.dto.CryptoMarketTick;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("CoinGecko Crypto Source Tests")
class CoinGeckoCryptoSourceTest {

    @Mock
    private CoinGeckoClient client;

    @Mock
    private CoinGeckoTickMapper adapter;

    @Mock
    private CoinGeckoProperties props;

    @Mock
    private CollectorProperties collectorProps;

    @InjectMocks
    private CoinGeckoCryptoSource source;

    @BeforeEach
    void setUp() {
        // Setup default properties mocks
        lenient().when(props.vsCurrency()).thenReturn("usd");
        lenient().when(props.perPage()).thenReturn(100);
        lenient().when(props.maxPages()).thenReturn(1);
        
        CollectorProperties.Kafka kafkaProps = mock(CollectorProperties.Kafka.class);
        lenient().when(kafkaProps.topicCrypto()).thenReturn("market.crypto");
        lenient().when(collectorProps.kafka()).thenReturn(kafkaProps);
        
        CollectorProperties.Schedule scheduleProps = mock(CollectorProperties.Schedule.class);
        lenient().when(scheduleProps.cryptoDelayMs()).thenReturn(20000L);
        lenient().when(scheduleProps.cryptoInitialDelayMs()).thenReturn(5000L);
        lenient().when(collectorProps.schedule()).thenReturn(scheduleProps);
    }

    @Test
    @DisplayName("Should fetch and map pages of data")
    void shouldFetchAndMapPages() {
        // Given
        CoinGeckoMarketItem item = new CoinGeckoMarketItem("bitcoin", "btc", null, null, null, null, null, null, null, null, null, null, null, null);
        
        List<CoinGeckoMarketItem> apiResponse = List.of(item);
        
        CryptoMarketTick tick = CryptoMarketTick.builder()
                .symbol("BTCUSD")
                .price(new BigDecimal("50000"))
                .source("COINGECKO")
                .fetchedAt(Instant.now())
                .build();

        when(client.fetchMarketsPage(1)).thenReturn(apiResponse);
        when(adapter.adapt(eq(item), eq("usd"), any(Instant.class))).thenReturn(tick);

        // When
        List<CryptoMarketTick> result = source.fetch();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getSymbol()).isEqualTo("BTCUSD");
        
        verify(client).fetchMarketsPage(1);
        verify(adapter).adapt(eq(item), eq("usd"), any(Instant.class));
    }

    @Test
    @DisplayName("Should fetch multiple pages if configured")
    void shouldFetchMultiplePages() {
        // Given
        when(props.maxPages()).thenReturn(2);
        when(props.perPage()).thenReturn(1);
        
        CoinGeckoMarketItem item1 = new CoinGeckoMarketItem("bitcoin", "btc", null, null, null, null, null, null, null, null, null, null, null, null);
        CoinGeckoMarketItem item2 = new CoinGeckoMarketItem("ethereum", "eth", null, null, null, null, null, null, null, null, null, null, null, null);
        
        CryptoMarketTick tick1 = CryptoMarketTick.builder().symbol("BTCUSD").build();
        CryptoMarketTick tick2 = CryptoMarketTick.builder().symbol("ETHUSD").build();

        when(client.fetchMarketsPage(1)).thenReturn(List.of(item1));
        when(client.fetchMarketsPage(2)).thenReturn(List.of(item2));
        
        when(adapter.adapt(eq(item1), eq("usd"), any(Instant.class))).thenReturn(tick1);
        when(adapter.adapt(eq(item2), eq("usd"), any(Instant.class))).thenReturn(tick2);

        // When
        List<CryptoMarketTick> result = source.fetch();

        // Then
        assertThat(result).hasSize(2);
        verify(client, times(2)).fetchMarketsPage(anyInt());
    }

    @Test
    @DisplayName("Should stop fetching early if a page is empty")
    void shouldStopFetchingWhenEmptyPage() {
        // Given
        when(props.maxPages()).thenReturn(5);
        when(props.perPage()).thenReturn(1);
        when(client.fetchMarketsPage(1)).thenReturn(List.of(new CoinGeckoMarketItem(null, null, null, null, null, null, null, null, null, null, null, null, null, null)));
        when(client.fetchMarketsPage(2)).thenReturn(List.of());
        when(adapter.adapt(any(), anyString(), any(Instant.class))).thenReturn(CryptoMarketTick.builder().build());

        // When
        source.fetch();

        // Then
        verify(client).fetchMarketsPage(1);
        verify(client).fetchMarketsPage(2);
        verify(client, never()).fetchMarketsPage(3);
    }

    @Test
    @DisplayName("Should return asset type CRYPTO and configuration properties correctly")
    void shouldReturnCorrectProperties() {
        assertThat(source.assetType()).isEqualTo(AssetType.CRYPTO);
        assertThat(source.sourceName()).isEqualTo("COINGECKO");
        assertThat(source.topicName()).isEqualTo("market.crypto");
        assertThat(source.delayMs()).isEqualTo(20000L);
        assertThat(source.initialDelayMs()).isEqualTo(5000L);
    }
}
