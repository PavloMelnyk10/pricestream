package com.pricestream.core.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.pricestream.core.domain.CryptoPrice;
import com.pricestream.core.domain.PagedResult;
import com.pricestream.core.exception.MarketDataException;
import com.pricestream.core.port.out.CryptoPriceReadPort;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CryptoPriceReadServiceTest {

    @Mock
    private CryptoPriceReadPort cryptoPriceReadPort;

    @InjectMocks
    private CryptoPriceReadService cryptoPriceReadService;

    @Test
    @DisplayName("Should return paged prices from port")
    void shouldReturnPagedPrices() {
        // Given
        int page = 1;
        int size = 10;
        PagedResult<CryptoPrice> expectedResult = new PagedResult<>(
                List.of(createPrice("BTC", "Bitcoin")),
                page, size, 1L, 1, true, true
        );
        given(cryptoPriceReadPort.findAll(page, size)).willReturn(expectedResult);

        // When
        PagedResult<CryptoPrice> result = cryptoPriceReadService.getCryptoPrices(page, size);

        // Then
        assertThat(result).isSameAs(expectedResult);
        then(cryptoPriceReadPort).should().findAll(page, size);
    }

    @Test
    @DisplayName("Should return latest price when symbol exists")
    void shouldReturnLatestPriceWhenSymbolExists() {
        // Given
        String symbol = "BTC";
        CryptoPrice expectedPrice = createPrice(symbol, "Bitcoin");
        given(cryptoPriceReadPort.findLatestBySymbol(symbol)).willReturn(Optional.of(expectedPrice));

        // When
        CryptoPrice result = cryptoPriceReadService.getLatestCryptoPrice(symbol);

        // Then
        assertThat(result).isSameAs(expectedPrice);
        then(cryptoPriceReadPort).should().findLatestBySymbol(symbol);
    }

    @Test
    @DisplayName("Should throw MarketDataException when latest price not found")
    void shouldThrowExceptionWhenLatestPriceNotFound() {
        // Given
        String symbol = "UNKNOWN";
        given(cryptoPriceReadPort.findLatestBySymbol(symbol)).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> cryptoPriceReadService.getLatestCryptoPrice(symbol))
                .isInstanceOf(MarketDataException.class)
                .hasMessage("No data found for symbol: " + symbol);
        
        then(cryptoPriceReadPort).should().findLatestBySymbol(symbol);
    }

    @Test
    @DisplayName("Should return price history from port")
    void shouldReturnPriceHistory() {
        // Given
        String symbol = "BTC";
        Instant from = Instant.now().minusSeconds(3600);
        Instant to = Instant.now();
        int limit = 50;
        List<CryptoPrice> expectedHistory = List.of(createPrice(symbol, "Bitcoin"));
        given(cryptoPriceReadPort.findHistory(symbol, from, to, limit)).willReturn(expectedHistory);

        // When
        List<CryptoPrice> result = cryptoPriceReadService.getCryptoPriceHistory(symbol, from, to, limit);

        // Then
        assertThat(result).isSameAs(expectedHistory);
        then(cryptoPriceReadPort).should().findHistory(symbol, from, to, limit);
    }

    @Test
    @DisplayName("Should return top crypto by market cap from port")
    void shouldReturnTopCrypto() {
        // Given
        int limit = 5;
        List<CryptoPrice> expectedTop = List.of(createPrice("BTC", "Bitcoin"), createPrice("ETH", "Ethereum"));
        given(cryptoPriceReadPort.findTopByMarketCap(limit)).willReturn(expectedTop);

        // When
        List<CryptoPrice> result = cryptoPriceReadService.getTopCryptoByMarketCap(limit);

        // Then
        assertThat(result).isSameAs(expectedTop);
        then(cryptoPriceReadPort).should().findTopByMarketCap(limit);
    }

    @Test
    @DisplayName("Should return all symbols from port")
    void shouldReturnAllSymbols() {
        // Given
        List<String> expectedSymbols = List.of("BTC", "ETH", "USDT");
        given(cryptoPriceReadPort.findAllSymbols()).willReturn(expectedSymbols);

        // When
        List<String> result = cryptoPriceReadService.getCryptoSymbols();

        // Then
        assertThat(result).isSameAs(expectedSymbols);
        then(cryptoPriceReadPort).should().findAllSymbols();
    }

    private CryptoPrice createPrice(String symbol, String name) {
        return CryptoPrice.builder()
                .symbol(symbol)
                .name(name)
                .baseAsset(symbol)
                .quoteAsset("USD")
                .price(BigDecimal.valueOf(50000))
                .marketCap(BigDecimal.valueOf(1000000000))
                .volume24h(BigDecimal.valueOf(500000000))
                .source("CoinGecko")
                .fetchedAt(Instant.now())
                .timestamp(Instant.now())
                .build();
    }
}
