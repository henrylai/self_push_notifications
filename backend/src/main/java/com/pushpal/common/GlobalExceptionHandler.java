package com.pushpal.common;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(IllegalArgumentException ex) {
        log.warn("Bad request: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(400, "Bad Request", ex.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleConflict(IllegalStateException ex) {
        log.warn("Conflict: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ErrorResponse.of(409, "Conflict", ex.getMessage()));
    }

    @ExceptionHandler(jakarta.persistence.EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(jakarta.persistence.EntityNotFoundException ex) {
        log.warn("Not found: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of(404, "Not Found", ex.getMessage()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleForbidden(AccessDeniedException ex) {
        log.warn("Forbidden request: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ErrorResponse.of(403, "Forbidden", "You are not allowed to perform this action"));
    }

    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ErrorResponse> handleRateLimit(RateLimitExceededException ex) {
        log.warn("Rate limit exceeded");
        return ResponseEntity
                .status(HttpStatus.TOO_MANY_REQUESTS)
                .body(ErrorResponse.of(429, "Too Many Requests", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getDefaultMessage())
                .distinct()
                .collect(Collectors.joining("; "));
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(400, "Validation Error", message));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(400, "Validation Error", ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex) {
        log.error("Internal server error", ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.of(500, "Internal Server Error",
                        "An unexpected error occurred"));
    }
}
