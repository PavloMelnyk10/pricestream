package com.pricestream.processor.normalizer;

import static org.assertj.core.api.Assertions.assertThat;

import com.pricestream.core.domain.AssetType;
import com.pricestream.core.dto.CryptoMarketTick;
import com.pricestream.processor.domain.CryptoPriceDocument;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

@DisplayName("Crypto Price Normalizer Tests")
class CryptoPriceNormalizerTest {

    private final CryptoPriceNormalizer normalizer = Mappers.getMapper(CryptoPriceNormalizer.class);

    @Test
    @DisplayName("Should correctly map CryptoMarketTick to CryptoPriceDocument")
    void shouldMapTickToDocument() {
        // Given
        Instant now = Instant.now();
        CryptoMarketTick tick = CryptoMarketTick.builder()
                .coinId("bitcoin")
                .symbol("BTCUSDT")
                .name("Bitcoin")
                .baseCurrency("BTC")
                .quoteCurrency("USDT")
                .price(new BigDecimal("60000.50"))
                .marketCap(new BigDecimal("1000000000"))
                .volume24h(new BigDecimal("50000000"))
                .priceChange24h(new BigDecimal("1500.00"))
                .priceChangePct24h(new BigDecimal("2.5"))
                .circulatingSupply(new BigDecimal("19000000"))
                .totalSupply(new BigDecimal("21000000"))
                .source("COINGECKO")
                .fetchedAt(now)
                .lastUpdated(now.minusSeconds(10))
                .build();

        // When
        CryptoPriceDocument doc = normalizer.normalize(tick);

        // Then
        assertThat(doc).isNotNull();
        assertThat(doc.getId()).isNull(); // ID should be ignored
        assertThat(doc.getSymbol()).isEqualTo("BTCUSDT");
        assertThat(doc.getName()).isEqualTo("Bitcoin");
        assertThat(doc.getBaseAsset()).isEqualTo("BTC");
        assertThat(doc.getQuoteAsset()).isEqualTo("USDT");
        assertThat(doc.getPrice()).isEqualTo(new BigDecimal("60000.50"));
        assertThat(doc.getMarketCap()).isEqualTo(new BigDecimal("1000000000"));
        assertThat(doc.getVolume24h()).isEqualTo(new BigDecimal("50000000"));
        assertThat(doc.getChange24h()).isEqualTo(new BigDecimal("1500.00"));
        assertThat(doc.getChangePct24h()).isEqualTo(new BigDecimal("2.5"));
        assertThat(doc.getCirculatingSupply()).isEqualTo(new BigDecimal("19000000"));
        assertThat(doc.getTotalSupply()).isEqualTo(new BigDecimal("21000000"));
        assertThat(doc.getSource()).isEqualTo("COINGECKO");
        assertThat(doc.getFetchedAt()).isEqualTo(now);
        assertThat(doc.getTimestamp()).isEqualTo(now.minusSeconds(10));
    }

    @Test
    @DisplayName("Should return CRYPTO asset type")
    void shouldReturnCryptoAssetType() {
        assertThat(normalizer.assetType()).isEqualTo(AssetType.CRYPTO);
    }
}
