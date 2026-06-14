package com.pricestream.processor.adapter.in.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pricestream.core.domain.AssetType;
import com.pricestream.core.dto.CryptoMarketTick;
import com.pricestream.core.port.out.MarketDataNormalizer;
import com.pricestream.core.port.out.MarketDataPersister;
import com.pricestream.processor.domain.CryptoPriceDocument;
import com.pricestream.processor.normalizer.MarketDataNormalizerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

/**
 * Kafka listener that consumes, normalizes, and persists crypto market data.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CryptoKafkaConsumer {

    private final ObjectMapper objectMapper;
    private final MarketDataNormalizerFactory normalizerFactory;
    private final MarketDataPersister<CryptoPriceDocument> persister;

    @KafkaListener(topics = "${collector.kafka.topic-crypto:market.crypto}")
    public void consume(String payload, Acknowledgment acknowledgment) {
        log.debug("Received crypto market data payload: {}", payload);
        try {
            // Deserialize
            CryptoMarketTick tick = objectMapper.readValue(payload, CryptoMarketTick.class);

            // Normalize
            MarketDataNormalizer<CryptoMarketTick, CryptoPriceDocument> normalizer =
                    normalizerFactory.getNormalizer(AssetType.CRYPTO);
            CryptoPriceDocument document = normalizer.normalize(tick);

            // Persist
            persister.save(document);

            // Commit offset
            acknowledgment.acknowledge();

        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize crypto payload. Skipping record. Payload: {}", payload, e);
            acknowledgment.acknowledge();
        }
    }
}
