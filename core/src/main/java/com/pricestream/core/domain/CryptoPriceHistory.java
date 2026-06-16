package com.pricestream.core.domain;

import java.util.List;

/**
 * Wrapper for a list of crypto prices to facilitate correct JSON serialization
 * and caching of generic collections.
 */
public record CryptoPriceHistory(
        List<CryptoPrice> content
) {
}
