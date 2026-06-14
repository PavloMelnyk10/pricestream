package com.pricestream.api.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.pricestream.api.dto.CryptoPriceResponse;
import com.pricestream.api.dto.PagedResponse;
import com.pricestream.core.domain.CryptoPrice;
import com.pricestream.core.domain.PagedResult;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

@DisplayName("Crypto Price Rest Mapper Tests")
class CryptoPriceRestMapperTest {

    private final CryptoPriceRestMapper mapper = Mappers.getMapper(CryptoPriceRestMapper.class);

    @Test
    @DisplayName("Should map domain model to response DTO")
    void shouldMapDomainToResponse() {
        // Given
        Instant now = Instant.now();
        CryptoPrice domain = CryptoPrice.builder()
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
        CryptoPriceResponse response = mapper.toResponse(domain);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.symbol()).isEqualTo("BTCUSD");
        assertThat(response.name()).isEqualTo("Bitcoin");
        assertThat(response.price()).isEqualTo(new BigDecimal("50000"));
        assertThat(response.marketCap()).isEqualTo(new BigDecimal("1000000000"));
        assertThat(response.volume24h()).isEqualTo(new BigDecimal("50000000"));
        assertThat(response.change24h()).isEqualTo(new BigDecimal("1000"));
        assertThat(response.changePct24h()).isEqualTo(new BigDecimal("2.0"));
        assertThat(response.circulatingSupply()).isEqualTo(new BigDecimal("19000000"));
        assertThat(response.totalSupply()).isEqualTo(new BigDecimal("21000000"));
        assertThat(response.source()).isEqualTo("COINGECKO");
        assertThat(response.fetchedAt()).isEqualTo(now);
        assertThat(response.timestamp()).isEqualTo(now);
    }

    @Test
    @DisplayName("Should map list of domain models to response list")
    void shouldMapDomainListToResponseList() {
        // Given
        CryptoPrice domain1 = CryptoPrice.builder().symbol("BTCUSD").build();
        CryptoPrice domain2 = CryptoPrice.builder().symbol("ETHUSD").build();

        // When
        List<CryptoPriceResponse> responses = mapper.toResponseList(List.of(domain1, domain2));

        // Then
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).symbol()).isEqualTo("BTCUSD");
        assertThat(responses.get(1).symbol()).isEqualTo("ETHUSD");
    }

    @Test
    @DisplayName("Should map PagedResult to PagedResponse")
    void shouldMapPagedResult() {
        // Given
        CryptoPrice domain = CryptoPrice.builder().symbol("BTCUSD").build();
        PagedResult<CryptoPrice> result = PagedResult.<CryptoPrice>builder()
                .content(List.of(domain))
                .page(0)
                .size(10)
                .totalElements(1)
                .totalPages(1)
                .first(true)
                .last(true)
                .build();

        // When
        PagedResponse<CryptoPriceResponse> response = mapper.toPagedResponse(result);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().getFirst().symbol()).isEqualTo("BTCUSD");
        assertThat(response.getPage()).isZero();
        assertThat(response.getSize()).isEqualTo(10);
        assertThat(response.getTotalElements()).isEqualTo(1);
        assertThat(response.getTotalPages()).isEqualTo(1);
        assertThat(response.isFirst()).isTrue();
        assertThat(response.isLast()).isTrue();
    }
}
