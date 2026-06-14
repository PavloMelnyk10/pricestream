package com.pricestream.app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.pricestream.collector.adapter.out.coingecko.CoinGeckoClient;
import java.util.Objects;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "api.rate-limit.enabled=true",
                "api.rate-limit.requests-per-window=5",
                "api.rate-limit.window-seconds=10"
        })
@Testcontainers
@DisplayName("Redis Rate Limiter Integration Tests")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class RedisRateLimiterIntegrationTest {

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:noble");

    @Container
    static KafkaContainer kafkaContainer = new KafkaContainer(DockerImageName.parse("apache/kafka:4.2.1"));

    @Container
    @SuppressWarnings("resource")
    static GenericContainer<?> redisContainer = new GenericContainer<>(DockerImageName.parse("redis:8.0-alpine"))
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
        registry.add("spring.kafka.bootstrap-servers", kafkaContainer::getBootstrapServers);
        registry.add("spring.data.redis.host", redisContainer::getHost);
        registry.add("spring.data.redis.port", redisContainer::getFirstMappedPort);
        registry.add("collector.schedule.crypto-delay-ms", () -> 60000);
        registry.add("collector.schedule.crypto-initial-delay-ms", () -> 60000);
    }

    @LocalServerPort
    private int port;

    private RestClient restClient;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @MockitoBean
    private CoinGeckoClient coinGeckoClient;

    @BeforeEach
    void setUp() {
        this.restClient = RestClient.builder()
                .requestFactory(new org.springframework.http.client.JdkClientHttpRequestFactory())
                .baseUrl("http://localhost:" + port)
                .build();

        Objects.requireNonNull(redisTemplate.getConnectionFactory()).getConnection().serverCommands().flushDb();
    }

    @Test
    @DisplayName("Should block requests exceeding the rate limit and return 429")
    void shouldEnforceRateLimit() {

        String endpoint = "/api/v1/market-data/crypto/symbols";

        for (int i = 0; i < 5; i++) {
            ResponseEntity<String> response = restClient.get()
                    .uri(endpoint)
                    .header("X-Forwarded-For", "192.168.1.55")
                    .retrieve()
                    .toEntity(String.class);
            
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getHeaders().getFirst("X-RateLimit-Limit")).isEqualTo("5");
        }

        RestClient.ResponseSpec responseSpec = restClient.get()
                .uri(endpoint)
                .header("X-Forwarded-For", "192.168.1.55")
                .retrieve();

        HttpClientErrorException exception = assertThrows(HttpClientErrorException.class, responseSpec::toBodilessEntity);

        assertThat(exception.getStatusCode().value()).isEqualTo(429);
        assertThat(exception.getResponseHeaders()).isNotNull();
        assertThat(exception.getResponseHeaders().getFirst("Retry-After")).isEqualTo("10");
        assertThat(exception.getResponseBodyAsString()).contains("Too Many Requests");

        ResponseEntity<String> diffIpResponse = restClient.get()
                .uri(endpoint)
                .header("X-Forwarded-For", "10.0.0.99")
                .retrieve()
                .toEntity(String.class);
                
        assertThat(diffIpResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
