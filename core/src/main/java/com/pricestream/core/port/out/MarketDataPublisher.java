package com.pricestream.core.port.out;

import com.pricestream.core.port.Publishable;
import java.util.List;

/**
 * Interface for publishing market data to a message broker.
 * It sends a list of publishable items to a specific topic.
 */
public interface MarketDataPublisher {

    int publishAll(String topic, List<? extends Publishable> ticks);
}
