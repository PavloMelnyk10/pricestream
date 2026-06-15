package com.pricestream.collector.scheduler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.pricestream.core.domain.AssetType;
import com.pricestream.core.dto.CryptoMarketTick;
import com.pricestream.core.port.out.MarketDataPublisher;
import com.pricestream.core.port.out.MarketDataSource;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.PeriodicTrigger;

@ExtendWith(MockitoExtension.class)
@DisplayName("Market Data Collector Scheduler Tests")
class MarketDataCollectorSchedulerTest {

    @Mock
    private MarketDataPublisher publisher;

    @Mock
    private TaskScheduler taskScheduler;

    @Mock
    private MarketDataSource<CryptoMarketTick> dataSource;

    @Captor
    private ArgumentCaptor<Runnable> taskCaptor;

    @Captor
    private ArgumentCaptor<PeriodicTrigger> triggerCaptor;

    @BeforeEach
    void setUp() {
        when(dataSource.sourceName()).thenReturn("TEST_SOURCE");
        when(dataSource.assetType()).thenReturn(AssetType.CRYPTO);
        when(dataSource.topicName()).thenReturn("test.topic");
        when(dataSource.delayMs()).thenReturn(20000L);
        when(dataSource.initialDelayMs()).thenReturn(5000L);

        new MarketDataCollectorScheduler(
                List.of(dataSource),
                publisher,
                taskScheduler
        );
    }

    @Test
    @DisplayName("Should schedule data sources on initialization")
    void shouldScheduleDataSources() {
        // Then
        verify(taskScheduler).schedule(taskCaptor.capture(), triggerCaptor.capture());

        PeriodicTrigger trigger = triggerCaptor.getValue();
        assertThat(trigger.getPeriodDuration().toMillis()).isEqualTo(20000L);
        assertThat(Objects.requireNonNull(trigger.getInitialDelayDuration()).toMillis()).isEqualTo(5000L);
    }

    @Test
    @DisplayName("Should fetch and publish ticks when scheduled task runs")
    void shouldFetchAndPublish() {
        // Given
        verify(taskScheduler).schedule(taskCaptor.capture(), any(PeriodicTrigger.class));
        Runnable scheduledTask = taskCaptor.getValue();

        CryptoMarketTick tick = CryptoMarketTick.builder()
                .symbol("BTCUSDT")
                .source("TEST_SOURCE")
                .fetchedAt(Instant.now())
                .build();
        List<CryptoMarketTick> ticks = List.of(tick);

        when(dataSource.fetch()).thenReturn(ticks);
        when(publisher.publishAll("test.topic", ticks)).thenReturn(1);

        // When
        scheduledTask.run();

        // Then
        verify(dataSource).fetch();
        verify(publisher).publishAll("test.topic", ticks);
    }

    @Test
    @DisplayName("Should not publish when fetch returns empty list")
    void shouldNotPublishWhenEmpty() {
        // Given
        verify(taskScheduler).schedule(taskCaptor.capture(), any(PeriodicTrigger.class));
        Runnable scheduledTask = taskCaptor.getValue();

        when(dataSource.fetch()).thenReturn(Collections.emptyList());

        // When
        scheduledTask.run();

        // Then
        verify(dataSource).fetch();
        verifyNoInteractions(publisher);
    }
}
