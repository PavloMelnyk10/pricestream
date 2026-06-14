package com.pricestream.api.config;

import com.pricestream.core.port.in.CryptoPriceReadUseCase;
import com.pricestream.core.port.out.CryptoPriceReadPort;
import com.pricestream.core.service.CryptoPriceReadService;
import org.jspecify.annotations.NonNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring configuration class for API layer beans and CORS settings.
 */
@Configuration
public class ApiConfig {

    @Bean
    public CryptoPriceReadUseCase cryptoPriceReadUseCase(CryptoPriceReadPort readPort) {
        return new CryptoPriceReadService(readPort);
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(@NonNull CorsRegistry registry) {
                registry.addMapping("/api/**")
                        .allowedOriginPatterns("http://localhost:*")
                        .allowedMethods("GET", "POST", "OPTIONS")
                        .allowedHeaders("*");
            }
        };
    }
}
