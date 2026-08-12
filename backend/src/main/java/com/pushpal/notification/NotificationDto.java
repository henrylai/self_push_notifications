package com.pushpal.notification;

import java.time.Instant;
import java.util.UUID;

public record NotificationDto(
        UUID id,
        UUID senderId,
        UUID recipientId,
        String title,
        String body,
        String icon,
        Instant scheduledTime,
        NotificationStatus status,
        Instant createdAt,
        Instant sentAt,
        Instant deliveredAt,
        Instant viewedAt,
        String failureReason
) {

    public static NotificationDto fromEntity(Notification notification) {
        return new NotificationDto(
                notification.getId(),
                notification.getSenderId(),
                notification.getRecipientId(),
                notification.getTitle(),
                notification.getBody(),
                notification.getIcon().apiValue(),
                notification.getScheduledTime(),
                notification.getStatus(),
                notification.getCreatedAt(),
                notification.getSentAt(),
                notification.getDeliveredAt(),
                notification.getViewedAt(),
                notification.getFailureReason()
        );
    }
}
