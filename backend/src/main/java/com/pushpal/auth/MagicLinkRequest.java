package com.pushpal.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MagicLinkRequest(
        @NotBlank(message = "Email is required")
        @Email(message = "A valid email is required")
        @Size(max = 255, message = "Email must be at most 255 characters")
        String email
) {}
