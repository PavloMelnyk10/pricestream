package com.pricestream.processor.adapter.in.kafka;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pricestream.core.domain.AssetType;
import com.pricestream.core.dto.CryptoMarketTick;
import com.pricestream.core.port.out.MarketDataNormalizer;
import com.pricestream.core.port.out.MarketDataPersister;
import com.pricestream.processor.domain.CryptoPriceDocument;
import com.pricestream.processor.normalizer.MarketDataNormalizerFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;

@ExtendWith(MockitoExtension.class)
@DisplayName("Crypto Kafka Consumer Tests")
class CryptoKafkaConsumerTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private MarketDataNormalizerFactory normalizerFactory;

    @Mock
    private MarketDataPersister<CryptoPriceDocument> persister;

    @Mock
    private MarketDataNormalizer<CryptoMarketTick, CryptoPriceDocument> normalizer;

    @Mock
    private Acknowledgment acknowledgment;

    @InjectMocks
    private CryptoKafkaConsumer consumer;

    private final String validPayload = "{\"symbol\":\"BTCUSDT\"}";

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    @DisplayName("Should successfully consume, normalize, and persist tick")
    void shouldConsumeSuccessfully() throws Exception {
        // Given
        CryptoMarketTick tick = CryptoMarketTick.builder().symbol("BTCUSDT").build();
        CryptoPriceDocument doc = new CryptoPriceDocument();
        doc.setSymbol("BTCUSDT");

        when(objectMapper.readValue(validPayload, CryptoMarketTick.class)).thenReturn(tick);
        when(normalizerFactory.getNormalizer(AssetType.CRYPTO)).thenReturn((MarketDataNormalizer) normalizer);
        when(normalizer.normalize(tick)).thenReturn(doc);

        // When
        consumer.consume(validPayload, acknowledgment);

        // Then
        verify(objectMapper).readValue(validPayload, CryptoMarketTick.class);
        verify(normalizerFactory).getNormalizer(AssetType.CRYPTO);
        verify(normalizer).normalize(tick);
        verify(persister).save(doc);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("Should acknowledge and skip message on JSON deserialization error (Poison Pill)")
    void shouldAcknowledgeOnJsonError() throws Exception {
        // Given
        String invalidPayload = "{invalid-json}";
        when(objectMapper.readValue(invalidPayload, CryptoMarketTick.class))
                .thenThrow(new JsonProcessingException("Test Error") {});

        // When
        consumer.consume(invalidPayload, acknowledgment);

        // Then
        verify(objectMapper).readValue(invalidPayload, CryptoMarketTick.class);
        verify(acknowledgment).acknowledge();
        verifyNoInteractions(normalizerFactory);
        verifyNoInteractions(persister);
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    @DisplayName("Should throw RuntimeException on processing error without acknowledging")
    void shouldThrowAndNotAcknowledgeOnProcessingError() throws Exception {
        // Given
        CryptoMarketTick tick = CryptoMarketTick.builder().symbol("BTCUSDT").build();

        when(objectMapper.readValue(validPayload, CryptoMarketTick.class)).thenReturn(tick);
        when(normalizerFactory.getNormalizer(AssetType.CRYPTO)).thenReturn((MarketDataNormalizer) normalizer);
        when(normalizer.normalize(tick)).thenThrow(new RuntimeException("Database down"));

        // When/Then
        assertThatThrownBy(() -> consumer.consume(validPayload, acknowledgment))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Database down");

        verify(acknowledgment, never()).acknowledge();
    }
}
