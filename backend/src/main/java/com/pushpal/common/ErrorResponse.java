package com.pushpal.common;

import java.time.Instant;
import java.util.UUID;

public record ErrorResponse(
        UUID id,
        int status,
        String error,
        String message,
        Instant timestamp
) {
    public static ErrorResponse of(int status, String error, String message) {
        return new ErrorResponse(UUID.randomUUID(), status, error, message, Instant.now());
    }
}
