package com.pricestream.core.port.out;

import com.pricestream.core.domain.AssetType;
import com.pricestream.core.port.Publishable;
import java.util.List;

/**
 * Interface representing an external source of market data.
 * It defines how to fetch data from the source and its basic properties.
 */
public interface MarketDataSource<T extends Publishable> extends SchedulableSource {

    /**
     * Fetch the latest market data from the external API.
     *
     * @return list of normalised market ticks; empty list on fallback/failure
     */
    List<T> fetch();

    /** The asset class this source provides. */
    AssetType assetType();

    /** Human-readable identifier, e.g. "COINGECKO". Used in logs and metrics. */
    String sourceName();
}
