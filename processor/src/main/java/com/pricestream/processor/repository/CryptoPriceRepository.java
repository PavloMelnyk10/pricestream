package com.pricestream.processor.repository;

import com.pricestream.processor.domain.CryptoPriceDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data MongoDB repository for crypto price documents.
 */
@Repository
public interface CryptoPriceRepository extends MongoRepository<CryptoPriceDocument, String> {

    /** Latest record for a given symbol (most recent timestamp) */
    Optional<CryptoPriceDocument> findTopBySymbolOrderByTimestampDesc(String symbol);

    /** All records for a symbol within a time range, ordered newest first */
    List<CryptoPriceDocument> findBySymbolAndTimestampBetweenOrderByTimestampDesc(
            String symbol, Instant from, Instant to);

    /** Paginated history for a symbol */
    Page<CryptoPriceDocument> findBySymbolOrderByTimestampDesc(String symbol, Pageable pageable);

    /** Top N distinct coins by most recent price, ordered by marketCap desc */
    @Query(value = "{}", sort = "{ 'marketCap': -1 }")
    List<CryptoPriceDocument> findTopByMarketCap(Pageable pageable);

    /** Get the latest record per symbol (for the "all coins" overview table) */
    @Query(value = "{ 'timestamp': { $gte: ?0 } }")
    List<CryptoPriceDocument> findAllSince(Instant since);

    /** All distinct symbols currently stored */
    @Query(value = "{}", fields = "{ 'symbol': 1 }")
    List<CryptoPriceDocument> findAllSymbols();

    /** All distinct symbols currently stored, sorted alphabetically */
    @Aggregation(pipeline = {
            "{ $group: { _id: '$symbol' } }",
            "{ $sort: { _id: 1 } }",
            "{ $project: { _id: 0, symbol: '$_id' } }"
    })
    List<String> findDistinctSymbolsSorted();
}
