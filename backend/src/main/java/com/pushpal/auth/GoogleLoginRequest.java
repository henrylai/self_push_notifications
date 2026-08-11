package com.pushpal.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record GoogleLoginRequest(
        @NotBlank(message = "Authorization code is required")
        @Size(max = 4096, message = "Authorization code is too long")
        String code,
        @NotBlank(message = "Redirect URI is required")
        @Size(max = 2048, message = "Redirect URI is too long")
        String redirectUri
) {}
