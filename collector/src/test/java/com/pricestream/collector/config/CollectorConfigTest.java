package com.pricestream.collector.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.web.reactive.function.client.WebClient;

@DisplayName("Collector Config Tests")
class CollectorConfigTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(CollectorConfig.class)
            .withPropertyValues(
                    "coingecko.base-url=https://api.coingecko.com/api/v3",
                    "coingecko.api-key=test-key",
                    "coingecko.vs-currency=usd",
                    "coingecko.per-page=100",
                    "coingecko.max-pages=1",
                    "collector.kafka.topic-crypto=market.crypto",
                    "collector.schedule.crypto-delay-ms=20000",
                    "collector.schedule.crypto-initial-delay-ms=5000"
            );

    @Test
    @DisplayName("Should load context and create all expected beans")
    void shouldCreateAllBeans() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(ObjectMapper.class);
            assertThat(context).hasSingleBean(ProducerFactory.class);
            assertThat(context).hasSingleBean(KafkaTemplate.class);
            assertThat(context).hasSingleBean(WebClient.class);
            assertThat(context).hasSingleBean(TaskScheduler.class);
        });
    }

    @Test
    @DisplayName("Should configure ObjectMapper with JavaTimeModule")
    void shouldConfigureObjectMapper() {
        contextRunner.run(context -> {
            ObjectMapper mapper = context.getBean(ObjectMapper.class);
            assertThat(mapper.getRegisteredModuleIds()).contains("jackson-datatype-jsr310");
        });
    }
}
