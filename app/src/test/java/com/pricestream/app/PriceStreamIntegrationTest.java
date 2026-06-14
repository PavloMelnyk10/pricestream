package com.pricestream.app;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.pricestream.api.dto.CryptoPriceResponse;
import java.util.concurrent.Callable;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.RestClient;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@DisplayName("PriceStream E2E Integration Test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class PriceStreamIntegrationTest {

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:noble");

    @Container
    static KafkaContainer kafkaContainer = new KafkaContainer(DockerImageName.parse("apache/kafka:4.2.1"));

    @Container
    @SuppressWarnings("resource")
    static GenericContainer<?> redisContainer = new GenericContainer<>(DockerImageName.parse("redis:8.0-alpine"))
            .withExposedPorts(6379);

    static WireMockServer wireMockServer;

    @BeforeAll
    static void startWireMock() {
        wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());
        wireMockServer.start();
        WireMock.configureFor("localhost", wireMockServer.port());
        
        // Mock CoinGecko API
        String mockResponse = """
                [
                  {
                    "id": "wiremockcoin",
                    "symbol": "wmc",
                    "name": "WireMockCoin",
                    "current_price": 61234.56,
                    "market_cap": 1180000000000.0,
                    "market_cap_rank": 1,
                    "total_volume": 40000000000.0,
                    "high_24h": 62000.0,
                    "low_24h": 60000.0,
                    "price_change_24h": 1234.56,
                    "price_change_percentage_24h": 2.05,
                    "circulating_supply": 19000000.0,
                    "total_supply": 21000000.0,
                    "last_updated": "2024-03-01T12:00:00.000Z"
                  }
                ]
                """;

        wireMockServer.stubFor(get(urlPathEqualTo("/coins/markets"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)
                        .withBody(mockResponse)));

        wireMockServer.stubFor(get(urlPathEqualTo("/coins/markets"))
                .withQueryParam("page", WireMock.equalTo("2"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)
                        .withBody("[]")));
    }

    @AfterAll
    static void stopWireMock() {
        if (wireMockServer != null) {
            wireMockServer.stop();
        }
    }

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
        registry.add("spring.kafka.bootstrap-servers", kafkaContainer::getBootstrapServers);
        registry.add("spring.data.redis.host", redisContainer::getHost);
        registry.add("spring.data.redis.port", redisContainer::getFirstMappedPort);

        registry.add("coingecko.base-url", wireMockServer::baseUrl);

        registry.add("collector.schedule.crypto-initial-delay-ms", () -> 100);
        registry.add("collector.schedule.crypto-delay-ms", () -> 60000);
    }

    @LocalServerPort
    private int port;

    private RestClient restClient;

    @BeforeEach
    void setUpClient() {
        this.restClient = RestClient.builder()
                .baseUrl("http://localhost:" + port)
                .build();
    }

    @Test
    @DisplayName("Should fetch from CoinGecko, process through Kafka, and serve via REST API")
    void shouldProcessFullFlowWithWireMock() {
        await().atMost(15, SECONDS)
               .pollInterval(1, SECONDS)
               .until(isCoinAvailableViaApi());

        ResponseEntity<CryptoPriceResponse> response = restClient.get()
                .uri("/api/v1/market-data/crypto/WMCUSD/latest")
                .retrieve()
                .toEntity(CryptoPriceResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().symbol()).isEqualTo("WMCUSD");
        assertThat(response.getBody().name()).isEqualTo("WireMockCoin");
        assertThat(response.getBody().price()).isEqualByComparingTo(new java.math.BigDecimal("61234.56"));
        assertThat(response.getBody().source()).isEqualTo("COINGECKO");
    }

    private Callable<Boolean> isCoinAvailableViaApi() {
        return () -> {
            try {
                ResponseEntity<CryptoPriceResponse> response = restClient.get()
                        .uri("/api/v1/market-data/crypto/" + "WMCUSD" + "/latest")
                        .retrieve()
                        .toEntity(CryptoPriceResponse.class);
                
                if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                    double actualPrice = response.getBody().price().doubleValue();
                    if (Math.abs(actualPrice - 61234.56) < 0.01) {
                        return true;
                    } else {
                        System.out.println("Price mismatch: expected " + 61234.56 + ", actual " + actualPrice);
                        return false;
                    }
                }
                return false;
            } catch (Exception _) {
                return false;
            }
        };
    }
}
