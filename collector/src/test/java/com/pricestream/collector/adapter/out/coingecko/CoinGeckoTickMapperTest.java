package com.pricestream.collector.adapter.out.coingecko;

import static org.assertj.core.api.Assertions.assertThat;

import com.pricestream.core.dto.CryptoMarketTick;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("CoinGecko Tick Mapper Tests")
class CoinGeckoTickMapperTest {

    private final CoinGeckoTickMapper mapper = new CoinGeckoTickMapper();

    @Test
    @DisplayName("Should map CoinGeckoMarketItem to CryptoMarketTick successfully")
    void shouldMapItemToTick() {
        // Given
        Instant now = Instant.now();
        Instant lastUpdated = now.minusSeconds(60);
        CoinGeckoMarketItem item = new CoinGeckoMarketItem(
                "bitcoin",
                "btc",
                "Bitcoin",
                new BigDecimal("50000.00"),
                new BigDecimal("1000000000.00"),
                1,
                new BigDecimal("50000000.00"),
                new BigDecimal("51000.00"),
                new BigDecimal("49000.00"),
                new BigDecimal("1000.00"),
                new BigDecimal("2.00"),
                new BigDecimal("19000000.00"),
                new BigDecimal("21000000.00"),
                lastUpdated
        );

        // When
        CryptoMarketTick tick = mapper.adapt(item, "usd", now);

        // Then
        assertThat(tick).isNotNull();
        assertThat(tick.getCoinId()).isEqualTo("bitcoin");
        assertThat(tick.getSymbol()).isEqualTo("BTCUSD");
        assertThat(tick.getName()).isEqualTo("Bitcoin");
        assertThat(tick.getBaseCurrency()).isEqualTo("BTC");
        assertThat(tick.getQuoteCurrency()).isEqualTo("USD");
        assertThat(tick.getPrice()).isEqualTo(new BigDecimal("50000.00"));
        assertThat(tick.getMarketCap()).isEqualTo(new BigDecimal("1000000000.00"));
        assertThat(tick.getMarketCapRank()).isEqualTo(1);
        assertThat(tick.getVolume24h()).isEqualTo(new BigDecimal("50000000.00"));
        assertThat(tick.getHigh24h()).isEqualTo(new BigDecimal("51000.00"));
        assertThat(tick.getLow24h()).isEqualTo(new BigDecimal("49000.00"));
        assertThat(tick.getPriceChange24h()).isEqualTo(new BigDecimal("1000.00"));
        assertThat(tick.getPriceChangePct24h()).isEqualTo(new BigDecimal("2.00"));
        assertThat(tick.getCirculatingSupply()).isEqualTo(new BigDecimal("19000000.00"));
        assertThat(tick.getTotalSupply()).isEqualTo(new BigDecimal("21000000.00"));
        assertThat(tick.getSource()).isEqualTo("COINGECKO");
        assertThat(tick.getFetchedAt()).isEqualTo(now);
        assertThat(tick.getLastUpdated()).isEqualTo(lastUpdated);
    }

    @Test
    @DisplayName("Should return null when input item is null")
    void shouldReturnNullWhenInputIsNull() {
        // When
        CryptoMarketTick tick = mapper.adapt(null, "usd", Instant.now());

        // Then
        assertThat(tick).isNull();
    }
}
