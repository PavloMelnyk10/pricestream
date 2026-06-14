package com.pricestream.api.exception;

/**
 * Data record representing a standard error response payload.
 */
public record ErrorResponse(
        String code,
        String message,
        String timestamp
) {
}
