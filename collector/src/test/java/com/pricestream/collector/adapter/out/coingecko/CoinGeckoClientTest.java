package com.pricestream.collector.adapter.out.coingecko;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import com.pricestream.collector.config.CoinGeckoProperties;
import java.util.List;
import java.util.function.Function;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

@ExtendWith(MockitoExtension.class)
@DisplayName("CoinGecko Client Tests")
class CoinGeckoClientTest {

    @Mock
    private WebClient webClient;

    @Mock
    private CoinGeckoProperties props;

    @SuppressWarnings("rawtypes")
    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @SuppressWarnings("rawtypes")
    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @InjectMocks
    private CoinGeckoClient client;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        lenient().when(props.vsCurrency()).thenReturn("usd");
        lenient().when(props.perPage()).thenReturn(100);

        lenient().when(webClient.get()).thenReturn(requestHeadersUriSpec);
        lenient().when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersSpec);
        lenient().when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        lenient().when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
    }

    @Test
    @DisplayName("Should fetch a page of markets successfully")
    void shouldFetchMarketsPageSuccessfully() {
        // Given
        CoinGeckoMarketItem item = new CoinGeckoMarketItem("bitcoin", "btc", null, null, null, null, null, null, null, null, null, null, null, null);
        when(responseSpec.bodyToFlux(CoinGeckoMarketItem.class)).thenReturn(Flux.just(item));

        // When
        List<CoinGeckoMarketItem> result = client.fetchMarketsPage(1);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().id()).isEqualTo("bitcoin");
    }
}
