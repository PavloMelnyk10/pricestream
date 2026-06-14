package com.pricestream.processor.adapter.out.db;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.pricestream.core.domain.MarketDataUpdatedEvent;
import com.pricestream.processor.domain.CryptoPriceDocument;
import com.pricestream.processor.repository.CryptoPriceRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DuplicateKeyException;

@ExtendWith(MockitoExtension.class)
@DisplayName("Crypto Price Persistence Adapter Tests")
class CryptoPricePersistenceAdapterTest {

    @Mock
    private CryptoPriceRepository repository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private CryptoPricePersistenceAdapter adapter;

    @Test
    @DisplayName("Should save document and publish event")
    void shouldSaveAndPublish() {
        // Given
        CryptoPriceDocument doc = CryptoPriceDocument.builder().symbol("BTCUSD").build();

        // When
        adapter.save(doc);

        // Then
        verify(repository).save(doc);
        verify(eventPublisher).publishEvent(any(MarketDataUpdatedEvent.class));
    }

    @Test
    @DisplayName("Should swallow DuplicateKeyException and not publish event")
    void shouldSwallowDuplicateKeyException() {
        // Given
        CryptoPriceDocument doc = CryptoPriceDocument.builder().symbol("BTCUSD").build();
        doThrow(new DuplicateKeyException("duplicate")).when(repository).save(any());

        // When
        // Then
        assertThatCode(() -> adapter.save(doc)).doesNotThrowAnyException();
        verifyNoInteractions(eventPublisher);
    }

    @Test
    @DisplayName("Should rethrow generic exception and not publish event")
    void shouldRethrowException() {
        // Given
        CryptoPriceDocument doc = CryptoPriceDocument.builder().symbol("BTCUSD").build();
        doThrow(new RuntimeException("db down")).when(repository).save(any());

        // When
        // Then
        assertThatThrownBy(() -> adapter.save(doc))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("db down");
        verifyNoInteractions(eventPublisher);
    }
}
