package com.pricestream.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for the PriceStream Spring Boot application.
 */
@SpringBootApplication(scanBasePackages = "com.pricestream")
public class Application {

    static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
