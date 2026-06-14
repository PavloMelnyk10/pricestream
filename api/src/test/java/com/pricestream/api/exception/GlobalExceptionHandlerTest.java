package com.pricestream.api.exception;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.NoSuchElementException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@DisplayName("Global Exception Handler Tests")
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("Should handle NoSuchElementException with 404 NOT_FOUND")
    void shouldHandleNotFound() {
        // Given
        NoSuchElementException ex = new NoSuchElementException("Coin not found");

        // When
        ResponseEntity<ErrorResponse> response = handler.handleNotFound(ex);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("ASSET_NOT_FOUND");
        assertThat(response.getBody().message()).isEqualTo("Coin not found");
        assertThat(response.getBody().timestamp()).isNotBlank();
    }

    @Test
    @DisplayName("Should handle IllegalArgumentException with 400 BAD_REQUEST")
    void shouldHandleBadRequest() {
        // Given
        IllegalArgumentException ex = new IllegalArgumentException("Invalid input");

        // When
        ResponseEntity<ErrorResponse> response = handler.handleBadRequest(ex);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("VALIDATION_ERROR");
        assertThat(response.getBody().message()).isEqualTo("Invalid input");
        assertThat(response.getBody().timestamp()).isNotBlank();
    }

    @Test
    @DisplayName("Should handle generic Exception with 500 INTERNAL_SERVER_ERROR")
    void shouldHandleInternalError() {
        // Given
        Exception ex = new RuntimeException("Database down");

        // When
        ResponseEntity<ErrorResponse> response = handler.handleInternal(ex);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("INTERNAL_ERROR");
        assertThat(response.getBody().message()).isEqualTo("An unexpected error occurred");
        assertThat(response.getBody().timestamp()).isNotBlank();
    }
}
