package com.pricestream.core.port.out;

/**
 * Interface for providing scheduling metadata for a data source.
 * It contains the topic name and the delays required for scheduled fetching.
 */
public interface SchedulableSource {

    /** The Kafka topic this source publishes to. */
    String topicName();

    /** Delay between fetch cycles in milliseconds. */
    long delayMs();

    /** Initial delay before the first fetch cycle in milliseconds. */
    long initialDelayMs();
}
