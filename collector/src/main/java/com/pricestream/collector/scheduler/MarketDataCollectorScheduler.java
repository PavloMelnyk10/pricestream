package com.pricestream.collector.scheduler;

import com.pricestream.core.port.Publishable;
import com.pricestream.core.port.out.MarketDataPublisher;
import com.pricestream.core.port.out.MarketDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.PeriodicTrigger;
import org.springframework.stereotype.Component;
import io.micrometer.core.instrument.MeterRegistry;

import java.time.Duration;
import java.util.List;

/**
 * Scheduler that orchestrates the data fetching cycles for all market data sources.
 */
@Slf4j
@Component
public class MarketDataCollectorScheduler {

    private final MarketDataPublisher publisher;
    private final MeterRegistry meterRegistry;

    public MarketDataCollectorScheduler(
            List<MarketDataSource<?>> sources,
            MarketDataPublisher publisher,
            TaskScheduler taskScheduler,
            MeterRegistry meterRegistry) {

        this.publisher = publisher;
        this.meterRegistry = meterRegistry;

        for (MarketDataSource<?> source : sources) {
            scheduleSource(source, taskScheduler);
        }
    }

    private <T extends Publishable> void scheduleSource(MarketDataSource<T> source, TaskScheduler taskScheduler) {
        PeriodicTrigger trigger = new PeriodicTrigger(Duration.ofMillis(source.delayMs()));
        trigger.setFixedRate(false);
        trigger.setInitialDelay(Duration.ofMillis(source.initialDelayMs()));

        taskScheduler.schedule(() -> executeFetchCycle(source), trigger);

        log.info("Scheduled {} ({}) → topic '{}' every {}ms (initial delay: {}ms)",
                source.sourceName(), source.assetType(), source.topicName(),
                source.delayMs(), source.initialDelayMs());
    }

    private <T extends Publishable> void executeFetchCycle(MarketDataSource<T> source) {
        String sourceName = source.sourceName();
        long start = System.currentTimeMillis();

        log.info("{} scheduler triggered — starting fetch cycle", sourceName);

        List<T> ticks = source.fetch();

        if (ticks.isEmpty()) {
            log.warn("{} fetch returned 0 ticks — circuit may be open or API unavailable",
                    sourceName);
            meterRegistry.counter("pricestream.fetch.empty", "source", sourceName).increment();
            return;
        }

        int published = publisher.publishAll(source.topicName(), ticks);
        meterRegistry.counter("pricestream.fetch.success", "source", sourceName).increment(published);

        log.info("{} cycle complete — fetched={} published={} durationMs={}",
                sourceName, ticks.size(), published, System.currentTimeMillis() - start);
    }
}
