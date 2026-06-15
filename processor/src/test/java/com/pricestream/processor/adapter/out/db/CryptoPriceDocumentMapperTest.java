package com.pricestream.processor.adapter.out.db;

import static org.assertj.core.api.Assertions.assertThat;

import com.pricestream.core.domain.CryptoPrice;
import com.pricestream.processor.domain.CryptoPriceDocument;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

@DisplayName("Crypto Price Document Mapper Tests")
class CryptoPriceDocumentMapperTest {

    private final CryptoPriceDocumentMapper mapper = Mappers.getMapper(CryptoPriceDocumentMapper.class);

    @Test
    @DisplayName("Should map document to domain model")
    void shouldMapDocumentToDomain() {
        // Given
        Instant now = Instant.now();
        CryptoPriceDocument doc = CryptoPriceDocument.builder()
                .id("123")
                .symbol("BTCUSD")
                .name("Bitcoin")
                .baseAsset("BTC")
                .quoteAsset("USD")
                .price(new BigDecimal("50000"))
                .marketCap(new BigDecimal("1000000000"))
                .volume24h(new BigDecimal("50000000"))
                .change24h(new BigDecimal("1000"))
                .changePct24h(new BigDecimal("2.0"))
                .circulatingSupply(new BigDecimal("19000000"))
                .totalSupply(new BigDecimal("21000000"))
                .source("COINGECKO")
                .fetchedAt(now)
                .timestamp(now)
                .build();

        // When
        CryptoPrice domain = mapper.toDomain(doc);

        // Then
        assertThat(domain).isNotNull();
        assertThat(domain.symbol()).isEqualTo("BTCUSD");
        assertThat(domain.name()).isEqualTo("Bitcoin");
        assertThat(domain.baseAsset()).isEqualTo("BTC");
        assertThat(domain.quoteAsset()).isEqualTo("USD");
        assertThat(domain.price()).isEqualTo(new BigDecimal("50000"));
        assertThat(domain.marketCap()).isEqualTo(new BigDecimal("1000000000"));
        assertThat(domain.volume24h()).isEqualTo(new BigDecimal("50000000"));
        assertThat(domain.change24h()).isEqualTo(new BigDecimal("1000"));
        assertThat(domain.changePct24h()).isEqualTo(new BigDecimal("2.0"));
        assertThat(domain.circulatingSupply()).isEqualTo(new BigDecimal("19000000"));
        assertThat(domain.totalSupply()).isEqualTo(new BigDecimal("21000000"));
        assertThat(domain.source()).isEqualTo("COINGECKO");
        assertThat(domain.fetchedAt()).isEqualTo(now);
        assertThat(domain.timestamp()).isEqualTo(now);
    }

    @Test
    @DisplayName("Should map list of documents to list of domain models")
    void shouldMapList() {
        // Given
        CryptoPriceDocument doc1 = CryptoPriceDocument.builder().symbol("BTCUSD").build();
        CryptoPriceDocument doc2 = CryptoPriceDocument.builder().symbol("ETHUSD").build();

        // When
        List<CryptoPrice> domains = mapper.toDomainList(List.of(doc1, doc2));

        // Then
        assertThat(domains).hasSize(2);
        assertThat(domains.get(0).symbol()).isEqualTo("BTCUSD");
        assertThat(domains.get(1).symbol()).isEqualTo("ETHUSD");
    }

    @Test
    @DisplayName("Should handle nulls")
    void shouldHandleNulls() {
        // When
        CryptoPrice domain = mapper.toDomain(null);
        List<CryptoPrice> domains = mapper.toDomainList(null);

        // Then
        assertThat(domain).isNull();
        assertThat(domains).isNull();
    }
}
