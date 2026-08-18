package com.pushpal.device;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterDeviceRequest(
        @NotBlank(message = "Endpoint is required")
        @Size(max = 4096, message = "Endpoint is too long")
        String endpoint,
        @NotBlank(message = "p256dh key is required")
        @Size(max = 512, message = "p256dh key is too long")
        String p256dh,
        @JsonProperty("auth")
        @NotBlank(message = "Auth key is required")
        @Size(max = 512, message = "Auth key is too long")
        String authKey,
        @Size(max = 2048, message = "User agent is too long")
        String userAgent,
        boolean reactivate
) {}
