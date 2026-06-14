package com.pricestream.core.port.in;

import com.pricestream.core.domain.CryptoPrice;
import com.pricestream.core.domain.PagedResult;
import java.time.Instant;
import java.util.List;

/**
 * Inbound port for reading market data.
 * It is used by the REST API adapters to fetch the necessary information.
 */
public interface CryptoPriceReadUseCase {

    PagedResult<CryptoPrice> getCryptoPrices(int page, int size);

    CryptoPrice getLatestCryptoPrice(String symbol);

    List<CryptoPrice> getCryptoPriceHistory(String symbol, Instant from, Instant to, int limit);

    List<CryptoPrice> getTopCryptoByMarketCap(int limit);

    List<String> getCryptoSymbols();
}
