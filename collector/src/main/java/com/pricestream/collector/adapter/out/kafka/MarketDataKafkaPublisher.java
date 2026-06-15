package com.pricestream.collector.adapter.out.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pricestream.core.port.Publishable;
import com.pricestream.core.port.out.MarketDataPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.List;

/**
 * Publisher that sends standardized market data to a Kafka topic.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MarketDataKafkaPublisher implements MarketDataPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Serializes and publishes a list of ticks to Kafka.
     */
    @Override
    public int publishAll(String topic, List<? extends Publishable> ticks) {
        int count = 0;
        for (Publishable tick : ticks) {
            try {
                String payload = objectMapper.writeValueAsString(tick);
                String idempotencyKey = buildIdempotencyKey(tick);

                Message<String> message = MessageBuilder.withPayload(payload)
                        .setHeader(KafkaHeaders.TOPIC, topic)
                        .setHeader(KafkaHeaders.KEY, tick.partitionKey())
                        .setHeader("X-Idempotency-Key", idempotencyKey)
                        .setHeader("X-Source", tick.source())
                        .build();

                kafkaTemplate.send(message)
                        .whenComplete((result, ex) -> {
                            if (ex != null) {
                                log.error("Failed to send {} to {}: {}",
                                        tick.partitionKey(), topic, ex.getMessage());
                            } else {
                                log.trace("Sent {} → {}@{}",
                                        tick.partitionKey(), topic,
                                        result.getRecordMetadata().offset());
                            }
                        });

                count++;
            } catch (JsonProcessingException e) {
                log.error("Serialization failed for {}: {}", tick.partitionKey(), e.getMessage());
            }
        }
        log.info("Enqueued {} records to topic '{}'", count, topic);
        return count;
    }

    /**
     * Generates a unique idempotency key for the tick to prevent duplicates.
     */
    private String buildIdempotencyKey(Publishable tick) {
        long fiveMinWindow = (tick.fetchedAt().toEpochMilli() / 300_000L) * 300_000L;
        String raw = tick.partitionKey() + "|" + fiveMinWindow + "|" + tick.source();
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256")
                    .digest(raw.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException _) {
            return Base64.getEncoder().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
        }
    }
}
