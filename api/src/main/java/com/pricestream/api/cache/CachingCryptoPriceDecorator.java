package com.pricestream.api.cache;

import com.pricestream.core.domain.CryptoPrice;
import com.pricestream.core.domain.PagedResult;
import com.pricestream.core.port.in.CryptoPriceReadUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

/**
 * Decorator that adds caching capabilities to the crypto price read use case.
 */
@Service
@Primary
@RequiredArgsConstructor
public class CachingCryptoPriceDecorator implements CryptoPriceReadUseCase {

    private final CryptoPriceReadUseCase delegate;

    @Override
    @Cacheable(value = "cryptoPrices", key = "#page + '-' + #size")
    public PagedResult<CryptoPrice> getCryptoPrices(int page, int size) {
        return delegate.getCryptoPrices(page, size);
    }

    @Override
    @Cacheable(value = "latestCryptoPrice", key = "#symbol")
    public CryptoPrice getLatestCryptoPrice(String symbol) {
        return delegate.getLatestCryptoPrice(symbol);
    }

    @Override
    @Cacheable(value = "cryptoPriceHistory",
               key = "#symbol + '-' + (#from != null ? #from.toEpochMilli() / 60000 : 0) + '-' + (#to != null ? #to.toEpochMilli() / 60000 : 0) + '-' + #limit")
    public List<CryptoPrice> getCryptoPriceHistory(
            String symbol, Instant from, Instant to, int limit) {
        return delegate.getCryptoPriceHistory(symbol, from, to, limit);
    }

    @Override
    public List<CryptoPrice> getTopCryptoByMarketCap(int limit) {
        return delegate.getTopCryptoByMarketCap(limit);
    }

    @Override
    @Cacheable(value = "cryptoSymbols")
    public List<String> getCryptoSymbols() {
        return delegate.getCryptoSymbols();
    }
}
