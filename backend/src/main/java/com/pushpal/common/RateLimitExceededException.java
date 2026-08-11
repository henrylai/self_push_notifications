package com.pushpal.common;

public class RateLimitExceededException extends RuntimeException {

    public RateLimitExceededException(String message) {
        super(message);
    }
}
