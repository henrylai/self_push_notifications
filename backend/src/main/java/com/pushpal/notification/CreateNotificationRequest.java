package com.pushpal.notification;

import java.time.Instant;
import java.util.UUID;

public record CreateNotificationRequest(
        String title,
        String body,
        UUID recipientId,
        Instant scheduledTime
) {}
