package com.pricestream.processor.adapter.out.db;

import com.pricestream.core.domain.CryptoPrice;
import com.pricestream.core.domain.PagedResult;
import com.pricestream.core.port.out.CryptoPriceReadPort;
import com.pricestream.processor.domain.CryptoPriceDocument;
import com.pricestream.processor.repository.CryptoPriceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Adapter implementing the read port to fetch crypto prices from MongoDB.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CryptoPriceReadAdapter implements CryptoPriceReadPort {

    private final CryptoPriceRepository cryptoPriceRepository;
    private final CryptoPriceDocumentMapper mapper;

    @Override
    public PagedResult<CryptoPrice> findAll(int page, int size) {
        Page<CryptoPriceDocument> mongoPage = cryptoPriceRepository.findAll(
                PageRequest.of(page, size,
                        Sort.by(Sort.Direction.DESC, "timestamp")));
        Page<CryptoPrice> responsePage = mongoPage.map(mapper::toDomain);
        return PagedResult.<CryptoPrice>builder()
                .content(responsePage.getContent())
                .page(responsePage.getNumber())
                .size(responsePage.getSize())
                .totalElements(responsePage.getTotalElements())
                .totalPages(responsePage.getTotalPages())
                .first(responsePage.isFirst())
                .last(responsePage.isLast())
                .build();
    }

    @Override
    public Optional<CryptoPrice> findLatestBySymbol(String symbol) {
        return cryptoPriceRepository
                .findTopBySymbolOrderByTimestampDesc(symbol.toUpperCase())
                .map(mapper::toDomain);
    }

    @Override
    public List<CryptoPrice> findHistory(String symbol, Instant from, Instant to, int limit) {
        Instant effectiveTo = to != null ? to : Instant.now();
        Instant effectiveFrom = from != null ? from : effectiveTo.minusSeconds(86400);

        return cryptoPriceRepository
                .findBySymbolAndTimestampBetweenOrderByTimestampDesc(
                        symbol.toUpperCase(), effectiveFrom, effectiveTo)
                .stream()
                .limit(limit > 0 ? limit : 200)
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<CryptoPrice> findTopByMarketCap(int limit) {
        int effectiveLimit = limit > 0 ? Math.min(limit, 100) : 10;
        return cryptoPriceRepository
                .findTopByMarketCap(PageRequest.of(0, effectiveLimit))
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> findAllSymbols() {
        return cryptoPriceRepository.findDistinctSymbolsSorted();
    }
}
