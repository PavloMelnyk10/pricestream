package com.pricestream.core.domain;

/**
 * Domain event published when market data is updated.
 * This can be used to trigger side effects like cache invalidation in a decoupled way.
 */
public record MarketDataUpdatedEvent(AssetType assetType) {
}
