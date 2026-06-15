package com.pricestream.processor.adapter.out.db;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pricestream.core.domain.CryptoPrice;
import com.pricestream.core.domain.PagedResult;
import com.pricestream.processor.domain.CryptoPriceDocument;
import com.pricestream.processor.repository.CryptoPriceRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
@DisplayName("Crypto Price Read Adapter Tests")
class CryptoPriceReadAdapterTest {

    @Mock
    private CryptoPriceRepository repository;

    @Mock
    private CryptoPriceDocumentMapper mapper;

    @InjectMocks
    private CryptoPriceReadAdapter adapter;

    @Test
    @DisplayName("Should find all items with pagination")
    void shouldFindAll() {
        // Given
        CryptoPriceDocument doc = CryptoPriceDocument.builder().symbol("BTCUSD").build();
        CryptoPrice domain = CryptoPrice.builder().symbol("BTCUSD").build();
        Page<CryptoPriceDocument> mongoPage = new PageImpl<>(List.of(doc));

        when(repository.findAll(any(PageRequest.class))).thenReturn(mongoPage);
        when(mapper.toDomain(doc)).thenReturn(domain);

        // When
        PagedResult<CryptoPrice> result = adapter.findAll(0, 10);

        // Then
        assertThat(result.content()).hasSize(1);
        assertThat(result.content().getFirst().symbol()).isEqualTo("BTCUSD");
        assertThat(result.totalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should find latest by symbol")
    void shouldFindLatestBySymbol() {
        // Given
        CryptoPriceDocument doc = CryptoPriceDocument.builder().symbol("BTCUSD").build();
        CryptoPrice domain = CryptoPrice.builder().symbol("BTCUSD").build();

        when(repository.findTopBySymbolOrderByTimestampDesc("BTCUSD")).thenReturn(Optional.of(doc));
        when(mapper.toDomain(doc)).thenReturn(domain);

        // When
        Optional<CryptoPrice> result = adapter.findLatestBySymbol("btcusd");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().symbol()).isEqualTo("BTCUSD");
        verify(repository).findTopBySymbolOrderByTimestampDesc("BTCUSD");
    }

    @Test
    @DisplayName("Should find history and fallback to default dates")
    void shouldFindHistory() {
        // Given
        CryptoPriceDocument doc = CryptoPriceDocument.builder().symbol("BTCUSD").build();
        CryptoPrice domain = CryptoPrice.builder().symbol("BTCUSD").build();

        when(repository.findBySymbolAndTimestampBetweenOrderByTimestampDesc(eq("BTCUSD"), any(Instant.class), any(Instant.class)))
                .thenReturn(List.of(doc));
        when(mapper.toDomain(doc)).thenReturn(domain);

        // When
        List<CryptoPrice> result = adapter.findHistory("btcusd", null, null, 10);

        // Then
        assertThat(result).hasSize(1);
        verify(repository).findBySymbolAndTimestampBetweenOrderByTimestampDesc(eq("BTCUSD"), any(Instant.class), any(Instant.class));
    }

    @Test
    @DisplayName("Should find top by market cap")
    void shouldFindTopByMarketCap() {
        // Given
        CryptoPriceDocument doc = CryptoPriceDocument.builder().symbol("BTCUSD").build();
        CryptoPrice domain = CryptoPrice.builder().symbol("BTCUSD").build();

        when(repository.findTopByMarketCap(any(PageRequest.class))).thenReturn(List.of(doc));
        when(mapper.toDomain(doc)).thenReturn(domain);

        // When
        List<CryptoPrice> result = adapter.findTopByMarketCap(10);

        // Then
        assertThat(result).hasSize(1);
        verify(repository).findTopByMarketCap(any(PageRequest.class));
    }

    @Test
    @DisplayName("Should enforce upper limit on top by market cap")
    void shouldEnforceLimitOnTopMarketCap() {
        // Given
        when(repository.findTopByMarketCap(any(PageRequest.class))).thenReturn(List.of());

        // When
        adapter.findTopByMarketCap(200); // Exceeds 100 max

        // Then
        verify(repository).findTopByMarketCap(PageRequest.of(0, 100));
    }

    @Test
    @DisplayName("Should find all symbols")
    void shouldFindAllSymbols() {
        // Given
        when(repository.findDistinctSymbolsSorted()).thenReturn(List.of("BTCUSD", "ETHUSD"));

        // When
        List<String> result = adapter.findAllSymbols();

        // Then
        assertThat(result).containsExactly("BTCUSD", "ETHUSD");
    }
}
