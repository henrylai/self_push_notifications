package com.pushpal.notification;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;

import java.time.Instant;
import java.util.UUID;

public record CreateNotificationRequest(
        @NotBlank(message = "Title is required")
        @Size(max = 100, message = "Title must be at most 100 characters")
        String title,
        @Size(max = 500, message = "Body must be at most 500 characters")
        String body,
        @Pattern(regexp = "bell|heart|star|check|calendar|gift",
                message = "Unsupported notification icon")
        String icon,
        UUID recipientId,
        @NotNull(message = "Scheduled time is required")
        @Future(message = "Scheduled time must be in the future")
        Instant scheduledTime
) {}
