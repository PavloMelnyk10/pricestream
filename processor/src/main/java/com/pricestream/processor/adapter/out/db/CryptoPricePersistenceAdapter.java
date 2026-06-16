package com.pricestream.processor.adapter.out.db;

import com.pricestream.core.domain.AssetType;
import com.pricestream.core.domain.MarketDataUpdatedEvent;
import com.pricestream.core.port.out.MarketDataPersister;
import com.pricestream.processor.domain.CryptoPriceDocument;
import com.pricestream.processor.repository.CryptoPriceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import io.micrometer.core.instrument.MeterRegistry;

/**
 * Adapter that implements the persister port to save crypto price documents to MongoDB.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CryptoPricePersistenceAdapter implements MarketDataPersister<CryptoPriceDocument> {

    private final CryptoPriceRepository cryptoPriceRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final MeterRegistry meterRegistry;

    @Override
    public void save(CryptoPriceDocument document) {
        try {
            cryptoPriceRepository.save(document);
            eventPublisher.publishEvent(new MarketDataUpdatedEvent(AssetType.CRYPTO));
            meterRegistry.counter("pricestream.records.stored", "assetType", "CRYPTO").increment();
            log.debug("Successfully saved CryptoPriceDocument for symbol: {}", document.getSymbol());
        } catch (DuplicateKeyException _) {
            log.debug("Duplicate record skipped for symbol: {} at timestamp: {}",
                    document.getSymbol(), document.getTimestamp());
        } catch (Exception e) {
            log.error("Failed to save CryptoPriceDocument for symbol: {}", document.getSymbol(), e);
            throw e;
        }
    }
}
