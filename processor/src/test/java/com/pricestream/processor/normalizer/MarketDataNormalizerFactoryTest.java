package com.pricestream.processor.normalizer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.pricestream.core.domain.AssetType;
import com.pricestream.core.port.out.MarketDataNormalizer;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Market Data Normalizer Factory Tests")
class MarketDataNormalizerFactoryTest {

    @Test
    @DisplayName("Should successfully register and retrieve normalizers")
    @SuppressWarnings({"rawtypes", "unchecked"})
    void shouldRegisterAndRetrieveNormalizers() {
        // Given
        MarketDataNormalizer cryptoNormalizer = mock(MarketDataNormalizer.class);
        when(cryptoNormalizer.assetType()).thenReturn(AssetType.CRYPTO);

        // When
        MarketDataNormalizerFactory factory = new MarketDataNormalizerFactory(List.of(cryptoNormalizer));
        MarketDataNormalizer retrieved = factory.getNormalizer(AssetType.CRYPTO);

        // Then
        assertThat(retrieved).isSameAs(cryptoNormalizer);
    }

    @Test
    @DisplayName("Should throw exception when duplicate normalizer is found")
    @SuppressWarnings({"rawtypes", "unchecked"})
    void shouldThrowOnDuplicateNormalizer() {
        // Given
        MarketDataNormalizer normalizer1 = mock(MarketDataNormalizer.class);
        when(normalizer1.assetType()).thenReturn(AssetType.CRYPTO);

        MarketDataNormalizer normalizer2 = mock(MarketDataNormalizer.class);
        when(normalizer2.assetType()).thenReturn(AssetType.CRYPTO);

        // When/Then
        List<MarketDataNormalizer<?, ?>> normalizers = List.of(normalizer1, normalizer2);
        assertThatThrownBy(() -> new MarketDataNormalizerFactory(normalizers))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Duplicate normalizer for asset type CRYPTO");
    }

    @Test
    @DisplayName("Should throw exception when requesting unknown normalizer")
    void shouldThrowOnUnknownNormalizer() {
        // Given
        MarketDataNormalizerFactory factory = new MarketDataNormalizerFactory(List.of());

        // When/Then
        assertThatThrownBy(() -> factory.getNormalizer(AssetType.CRYPTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("No normalizer found for asset type: CRYPTO");
    }
}
