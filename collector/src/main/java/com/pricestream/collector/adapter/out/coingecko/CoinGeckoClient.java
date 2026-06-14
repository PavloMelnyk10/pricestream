package com.pricestream.collector.adapter.out.coingecko;

import com.pricestream.collector.config.CoinGeckoProperties;
import com.pricestream.core.exception.MarketDataException;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;

/**
 * HTTP client for fetching market data from the CoinGecko API.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CoinGeckoClient {

    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(30);

    private final WebClient coinGeckoWebClient;
    private final CoinGeckoProperties props;

    /**
     * Fetches a single page of market items from CoinGecko.
     */
    @RateLimiter(name = "coingecko")
    @Retry(name = "coingecko")
    public List<CoinGeckoMarketItem> fetchMarketsPage(int page) {
        log.debug("GET /coins/markets page={} vsCurrency={} perPage={}",
                page, props.vsCurrency(), props.perPage());

        List<CoinGeckoMarketItem> result = coinGeckoWebClient.get()
                .uri(uri -> uri.path("/coins/markets")
                        .queryParam("vs_currency", props.vsCurrency())
                        .queryParam("order", "market_cap_desc")
                        .queryParam("per_page", props.perPage())
                        .queryParam("page", page)
                        .queryParam("price_change_percentage", "24h")
                        .build())
                .retrieve()
                .onStatus(status -> status.value() == 429,
                        _ -> {
                            log.warn("CoinGecko rate limit hit (429) on page {}", page);
                            return Mono.error(new MarketDataException(
                                    "CoinGecko rate limit exceeded (HTTP 429)"));
                        })
                .onStatus(HttpStatusCode::is5xxServerError,
                        response -> Mono.error(new MarketDataException(
                                "CoinGecko server error: " + response.statusCode())))
                .bodyToFlux(CoinGeckoMarketItem.class)
                .collectList()
                .block(REQUEST_TIMEOUT);

        log.debug("CoinGecko page={} returned {} items", page, result != null ? result.size() : 0);
        return result != null ? result : List.of();
    }
}
