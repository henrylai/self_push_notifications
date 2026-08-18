package com.pushpal.notification;

import java.time.Instant;
import java.util.UUID;

public record NotificationDto(
        UUID id,
        UUID senderId,
        UUID recipientId,
        String senderName,
        String recipientName,
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
        return fromEntity(notification, null, null);
    }

    public static NotificationDto fromEntity(Notification notification,
                                             String senderName,
                                             String recipientName) {
        return new NotificationDto(
                notification.getId(),
                notification.getSenderId(),
                notification.getRecipientId(),
                senderName,
                recipientName,
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
