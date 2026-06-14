package com.pricestream.collector.adapter.out.kafka;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pricestream.core.dto.CryptoMarketTick;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.support.SendResult;
import org.springframework.messaging.Message;

@ExtendWith(MockitoExtension.class)
@DisplayName("Market Data Kafka Publisher Tests")
class MarketDataKafkaPublisherTest {

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private MarketDataKafkaPublisher publisher;

    @Captor
    private ArgumentCaptor<Message<String>> messageCaptor;

    @Test
    @DisplayName("Should publish tick to Kafka successfully")
    void shouldPublishTickSuccessfully() throws JsonProcessingException {
        // Given
        Instant now = Instant.now();
        CryptoMarketTick tick = CryptoMarketTick.builder()
                .symbol("BTCUSD")
                .source("COINGECKO")
                .fetchedAt(now)
                .build();
        
        String jsonPayload = "{\"symbol\":\"BTCUSD\"}";
        when(objectMapper.writeValueAsString(tick)).thenReturn(jsonPayload);
        
        CompletableFuture<SendResult<String, String>> future = CompletableFuture.completedFuture(null);
        when(kafkaTemplate.send(any(Message.class))).thenReturn(future);

        // When
        int publishedCount = publisher.publishAll("market.crypto", List.of(tick));

        // Then
        assertThat(publishedCount).isEqualTo(1);
        
        verify(kafkaTemplate).send(messageCaptor.capture());
        Message<String> sentMessage = messageCaptor.getValue();
        
        assertThat(sentMessage.getPayload()).isEqualTo(jsonPayload);
        assertThat(sentMessage.getHeaders()).containsEntry(KafkaHeaders.TOPIC, "market.crypto");
        assertThat(sentMessage.getHeaders()).containsEntry(KafkaHeaders.KEY, tick.partitionKey());
        assertThat(sentMessage.getHeaders()).containsEntry("X-Source", "COINGECKO");
        assertThat(sentMessage.getHeaders().get("X-Idempotency-Key")).isNotNull();
    }

    @Test
    @DisplayName("Should handle serialization failure gracefully")
    void shouldHandleSerializationFailure() throws JsonProcessingException {
        // Given
        CryptoMarketTick tick = CryptoMarketTick.builder()
                .symbol("BTCUSD")
                .source("COINGECKO")
                .fetchedAt(Instant.now())
                .build();
        
        when(objectMapper.writeValueAsString(tick)).thenThrow(new JsonProcessingException("Serialization failed") {});

        // When
        int publishedCount = publisher.publishAll("market.crypto", List.of(tick));

        // Then
        assertThat(publishedCount).isZero();
        verifyNoInteractions(kafkaTemplate);
    }
}
