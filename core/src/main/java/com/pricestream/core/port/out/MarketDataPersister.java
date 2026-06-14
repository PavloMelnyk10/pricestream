package com.pricestream.core.port.out;

/**
 * Interface for saving market data to a database.
 * It defines the contract for persisting normalized market data documents.
 */
public interface MarketDataPersister<T> {
    void save(T document);
}
