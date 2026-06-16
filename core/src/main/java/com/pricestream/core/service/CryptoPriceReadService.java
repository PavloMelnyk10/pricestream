package com.pricestream.core.service;

import com.pricestream.core.domain.CryptoPrice;
import com.pricestream.core.domain.CryptoPriceHistory;
import com.pricestream.core.domain.PagedResult;
import com.pricestream.core.exception.MarketDataException;
import com.pricestream.core.port.in.CryptoPriceReadUseCase;
import com.pricestream.core.port.out.CryptoPriceReadPort;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;

/**
 * Service that implements the read use-case for market data.
 * It provides the business logic for retrieving cryptocurrency prices.
 */
@RequiredArgsConstructor
public class CryptoPriceReadService implements CryptoPriceReadUseCase {

    private final CryptoPriceReadPort cryptoPriceReadPort;

    @Override
    public PagedResult<CryptoPrice> getCryptoPrices(int page, int size) {
        return cryptoPriceReadPort.findAll(page, size);
    }

    @Override
    public CryptoPrice getLatestCryptoPrice(String symbol) {
        return cryptoPriceReadPort.findLatestBySymbol(symbol)
                .orElseThrow(() -> new MarketDataException(
                        "No data found for symbol: " + symbol));
    }

    @Override
    public CryptoPriceHistory getCryptoPriceHistory(
            String symbol, Instant from, Instant to, int limit) {
        List<CryptoPrice> history = cryptoPriceReadPort.findHistory(symbol, from, to, limit);
        return new CryptoPriceHistory(history);
    }

    @Override
    public List<CryptoPrice> getTopCryptoByMarketCap(int limit) {
        return cryptoPriceReadPort.findTopByMarketCap(limit);
    }

    @Override
    public List<String> getCryptoSymbols() {
        return cryptoPriceReadPort.findAllSymbols();
    }
}
