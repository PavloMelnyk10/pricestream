package com.pricestream.api.controller;

import com.pricestream.api.dto.CryptoPriceResponse;
import com.pricestream.api.dto.PagedResponse;
import com.pricestream.api.mapper.CryptoPriceRestMapper;
import com.pricestream.core.domain.CryptoPrice;
import com.pricestream.core.domain.PagedResult;
import com.pricestream.core.port.in.CryptoPriceReadUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;

/**
 * REST controller providing endpoints for cryptocurrency market data.
 */
@RestController
@RequestMapping("/api/v1/market-data/crypto")
@RequiredArgsConstructor
public class CryptoPriceController {

    private final CryptoPriceReadUseCase cryptoPriceReadUseCase;
    private final CryptoPriceRestMapper mapper;

    @GetMapping
    public ResponseEntity<PagedResponse<CryptoPriceResponse>> getCryptoPrices(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PagedResult<CryptoPrice> result = cryptoPriceReadUseCase.getCryptoPrices(page, size);
        return ResponseEntity.ok(mapper.toPagedResponse(result));
    }

    @GetMapping("/{symbol}/latest")
    public ResponseEntity<CryptoPriceResponse> getLatestPrice(
            @PathVariable String symbol) {
        return ResponseEntity.ok(mapper.toResponse(cryptoPriceReadUseCase.getLatestCryptoPrice(symbol)));
    }

    @GetMapping("/{symbol}/history")
    public ResponseEntity<List<CryptoPriceResponse>> getPriceHistory(
            @PathVariable String symbol,
            @RequestParam(required = false) Instant from,
            @RequestParam(required = false) Instant to,
            @RequestParam(defaultValue = "100") int limit) {
        List<CryptoPrice> history = cryptoPriceReadUseCase.getCryptoPriceHistory(symbol, from, to, limit);
        return ResponseEntity.ok(mapper.toResponseList(history));
    }

    @GetMapping("/top")
    public ResponseEntity<List<CryptoPriceResponse>> getTopByMarketCap(
            @RequestParam(defaultValue = "10") int limit) {
        List<CryptoPrice> top = cryptoPriceReadUseCase.getTopCryptoByMarketCap(limit);
        return ResponseEntity.ok(mapper.toResponseList(top));
    }

    @GetMapping("/symbols")
    public ResponseEntity<List<String>> getSymbols() {
        return ResponseEntity.ok(cryptoPriceReadUseCase.getCryptoSymbols());
    }
}
