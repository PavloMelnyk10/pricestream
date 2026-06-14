package com.pricestream.core.port.out;

import com.pricestream.core.domain.CryptoPrice;
import com.pricestream.core.domain.PagedResult;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Outbound port for reading cryptocurrency price data from the database.
 * It defines the methods used to query the stored market data.
 */
public interface CryptoPriceReadPort {

    /** Paginated list of all crypto price records, sorted by timestamp desc. */
    PagedResult<CryptoPrice> findAll(int page, int size);

    /** Latest price record for a given symbol. */
    Optional<CryptoPrice> findLatestBySymbol(String symbol);

    /** Price history for a symbol within a time range (for charts). */
    List<CryptoPrice> findHistory(String symbol, Instant from, Instant to, int limit);

    /** Top N coins by market cap. */
    List<CryptoPrice> findTopByMarketCap(int limit);

    /** List of all available symbol strings. */
    List<String> findAllSymbols();
}
