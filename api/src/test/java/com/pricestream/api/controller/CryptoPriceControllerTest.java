package com.pricestream.api.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pricestream.api.dto.CryptoPriceResponse;
import com.pricestream.api.dto.PagedResponse;
import com.pricestream.api.mapper.CryptoPriceRestMapper;
import com.pricestream.core.domain.CryptoPrice;
import com.pricestream.core.domain.CryptoPriceHistory;
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
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
@DisplayName("Crypto Price Controller Tests")
class CryptoPriceControllerTest {

    @Mock
    private CryptoPriceReadUseCase useCase;

    @Mock
    private CryptoPriceRestMapper mapper;

    @InjectMocks
    private CryptoPriceController controller;

    @Test
    @DisplayName("Should get crypto prices")
    void shouldGetCryptoPrices() {
        // Given
        PagedResult<CryptoPrice> domainResult = PagedResult.<CryptoPrice>builder().build();
        PagedResponse<CryptoPriceResponse> expectedResponse = new PagedResponse<>();
        
        when(useCase.getCryptoPrices(0, 20)).thenReturn(domainResult);
        when(mapper.toPagedResponse(domainResult)).thenReturn(expectedResponse);

        // When
        ResponseEntity<PagedResponse<CryptoPriceResponse>> response = controller.getCryptoPrices(0, 20);

        // Then
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isSameAs(expectedResponse);
        verify(useCase).getCryptoPrices(0, 20);
    }

    @Test
    @DisplayName("Should get latest price")
    void shouldGetLatestPrice() {
        // Given
        CryptoPrice domain = CryptoPrice.builder().build();
        CryptoPriceResponse expectedResponse = CryptoPriceResponse.builder().build();
        
        when(useCase.getLatestCryptoPrice("BTCUSD")).thenReturn(domain);
        when(mapper.toResponse(domain)).thenReturn(expectedResponse);

        // When
        ResponseEntity<CryptoPriceResponse> response = controller.getLatestPrice("BTCUSD");

        // Then
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isSameAs(expectedResponse);
    }

    @Test
    @DisplayName("Should get price history")
    void shouldGetPriceHistory() {
        // Given
        List<CryptoPrice> domainList = List.of();
        CryptoPriceHistory wrapper = new CryptoPriceHistory(domainList);
        List<CryptoPriceResponse> expectedResponse = List.of();
        Instant from = Instant.now();
        Instant to = Instant.now();
        
        when(useCase.getCryptoPriceHistory("BTCUSD", from, to, 100)).thenReturn(wrapper);
        when(mapper.toResponseList(domainList)).thenReturn(expectedResponse);

        // When
        ResponseEntity<List<CryptoPriceResponse>> response = controller.getPriceHistory("BTCUSD", from, to, 100);

        // Then
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isSameAs(expectedResponse);
    }

    @Test
    @DisplayName("Should get top by market cap")
    void shouldGetTopByMarketCap() {
        // Given
        List<CryptoPrice> domainList = List.of();
        List<CryptoPriceResponse> expectedResponse = List.of();
        
        when(useCase.getTopCryptoByMarketCap(10)).thenReturn(domainList);
        when(mapper.toResponseList(domainList)).thenReturn(expectedResponse);

        // When
        ResponseEntity<List<CryptoPriceResponse>> response = controller.getTopByMarketCap(10);

        // Then
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isSameAs(expectedResponse);
    }

    @Test
    @DisplayName("Should get symbols")
    void shouldGetSymbols() {
        // Given
        List<String> symbols = List.of("BTCUSD", "ETHUSD");
        when(useCase.getCryptoSymbols()).thenReturn(symbols);

        // When
        ResponseEntity<List<String>> response = controller.getSymbols();

        // Then
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isEqualTo(symbols);
    }
}
