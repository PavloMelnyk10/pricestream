package com.pricestream.core.port;

import java.time.Instant;

/**
 * Interface for data transfer objects that can be published to a message broker.
 * It provides the minimum required fields for routing and identifying messages.
 */
public interface Publishable {

    /** Kafka partition key - typically the trading pair symbol (e.g. "BTCUSDT"). */
    String partitionKey();

    /** Identifier of the external API that produced this record (e.g. "COINGECKO"). */
    String source();

    /** Timestamp when this data was fetched from the external API. */
    Instant fetchedAt();
}
