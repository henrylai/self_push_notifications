package com.pushpal.device;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UnregisterDeviceRequest(
        @NotBlank(message = "Endpoint is required")
        @Size(max = 4096, message = "Endpoint is too long")
        String endpoint
) {}
