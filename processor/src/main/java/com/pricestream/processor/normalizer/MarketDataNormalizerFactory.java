package com.pricestream.processor.normalizer;

import com.pricestream.core.domain.AssetType;
import com.pricestream.core.port.out.MarketDataNormalizer;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Factory that provides the appropriate normalizer instance based on the asset type.
 */
@Component
public class MarketDataNormalizerFactory {

    private final Map<AssetType, MarketDataNormalizer<?, ?>> normalizers = new EnumMap<>(AssetType.class);

    public MarketDataNormalizerFactory(List<MarketDataNormalizer<?, ?>> normalizerList) {
        normalizerList.forEach(n -> {
            MarketDataNormalizer<?, ?> existing = normalizers.put(n.assetType(), n);
            if (existing != null) {
                throw new IllegalStateException(
                        "Duplicate normalizer for asset type " + n.assetType()
                                + ": " + existing.getClass().getSimpleName()
                                + " and " + n.getClass().getSimpleName());
            }
        });
    }

    @SuppressWarnings("unchecked")
    public <S, T> MarketDataNormalizer<S, T> getNormalizer(AssetType assetType) {
        MarketDataNormalizer<?, ?> normalizer = normalizers.get(assetType);
        if (normalizer == null) {
            throw new IllegalArgumentException("No normalizer found for asset type: " + assetType);
        }
        return (MarketDataNormalizer<S, T>) normalizer;
    }
}
