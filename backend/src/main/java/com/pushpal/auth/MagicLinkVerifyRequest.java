package com.pushpal.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MagicLinkVerifyRequest(
        @NotBlank(message = "Token is required")
        @Size(max = 256, message = "Token is too long")
        String token
) {}
