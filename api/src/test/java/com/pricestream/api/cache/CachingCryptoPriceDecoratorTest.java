package com.pricestream.api.cache;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pricestream.core.domain.CryptoPrice;
import com.pricestream.core.domain.PagedResult;
import com.pricestream.core.port.in.CryptoPriceReadUseCase;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("Caching Crypto Price Decorator Tests")
class CachingCryptoPriceDecoratorTest {

    @Mock
    private CryptoPriceReadUseCase delegate;

    @InjectMocks
    private CachingCryptoPriceDecorator decorator;

    @Test
    @DisplayName("Should delegate getCryptoPrices")
    void shouldDelegateGetCryptoPrices() {
        // Given
        PagedResult<CryptoPrice> expected = PagedResult.<CryptoPrice>builder().build();
        when(delegate.getCryptoPrices(0, 10)).thenReturn(expected);

        // When
        PagedResult<CryptoPrice> result = decorator.getCryptoPrices(0, 10);

        // Then
        assertThat(result).isSameAs(expected);
        verify(delegate).getCryptoPrices(0, 10);
    }

    @Test
    @DisplayName("Should delegate getLatestCryptoPrice")
    void shouldDelegateGetLatestCryptoPrice() {
        // Given
        CryptoPrice expected = CryptoPrice.builder().build();
        when(delegate.getLatestCryptoPrice("BTCUSD")).thenReturn(expected);

        // When
        CryptoPrice result = decorator.getLatestCryptoPrice("BTCUSD");

        // Then
        assertThat(result).isSameAs(expected);
        verify(delegate).getLatestCryptoPrice("BTCUSD");
    }

    @Test
    @DisplayName("Should delegate getCryptoPriceHistory")
    void shouldDelegateGetCryptoPriceHistory() {
        // Given
        List<CryptoPrice> expected = List.of();
        Instant from = Instant.now();
        Instant to = Instant.now();
        when(delegate.getCryptoPriceHistory("BTCUSD", from, to, 100)).thenReturn(expected);

        // When
        List<CryptoPrice> result = decorator.getCryptoPriceHistory("BTCUSD", from, to, 100);

        // Then
        assertThat(result).isSameAs(expected);
        verify(delegate).getCryptoPriceHistory("BTCUSD", from, to, 100);
    }

    @Test
    @DisplayName("Should delegate getTopCryptoByMarketCap")
    void shouldDelegateGetTopCryptoByMarketCap() {
        // Given
        List<CryptoPrice> expected = List.of();
        when(delegate.getTopCryptoByMarketCap(10)).thenReturn(expected);

        // When
        List<CryptoPrice> result = decorator.getTopCryptoByMarketCap(10);

        // Then
        assertThat(result).isSameAs(expected);
        verify(delegate).getTopCryptoByMarketCap(10);
    }

    @Test
    @DisplayName("Should delegate getCryptoSymbols")
    void shouldDelegateGetCryptoSymbols() {
        // Given
        List<String> expected = List.of("BTCUSD", "ETHUSD");
        when(delegate.getCryptoSymbols()).thenReturn(expected);

        // When
        List<String> result = decorator.getCryptoSymbols();

        // Then
        assertThat(result).isSameAs(expected);
        verify(delegate).getCryptoSymbols();
    }
}
