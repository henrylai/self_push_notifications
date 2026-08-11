package com.pushpal.device;

import java.time.Instant;
import java.util.UUID;

public record DeviceDto(
        UUID id,
        String userAgent,
        Instant createdAt,
        Instant lastUsedAt
) {

    public static DeviceDto fromEntity(PushSubscription subscription) {
        return new DeviceDto(
                subscription.getId(),
                subscription.getUserAgent(),
                subscription.getCreatedAt(),
                subscription.getLastUsedAt());
    }
}
