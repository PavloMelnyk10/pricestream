package com.pricestream.core.port.out;

import com.pricestream.core.domain.AssetType;

/**
 * Interface for normalizing raw market data into a standardized format.
 * It converts data from a specific source into a common representation.
 */
public interface MarketDataNormalizer<T, R> {
    R normalize(T rawData);

    AssetType assetType();
}

