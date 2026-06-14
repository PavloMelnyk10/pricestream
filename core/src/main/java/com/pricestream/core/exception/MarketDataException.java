package com.pricestream.core.exception;

/**
 * Exception for market data collection and processing failures.
 */
public class MarketDataException extends RuntimeException {

    public MarketDataException(String message) {
        super(message);
    }

    public MarketDataException(String message, Throwable cause) {
        super(message, cause);
    }
}
