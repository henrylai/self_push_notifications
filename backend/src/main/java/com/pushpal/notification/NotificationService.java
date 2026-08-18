package com.pushpal.notification;

import com.pushpal.common.RateLimitService;
import com.pushpal.relationship.RelationshipService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private static final int MAX_NOTIFICATIONS_PER_HOUR = 10;
    private static final long RETRY_DELAY_SECONDS = 60;
    private static final int MAX_TITLE_LENGTH = 100;
    private static final int MAX_BODY_LENGTH = 500;

    private final NotificationRepository notificationRepository;
    private final RelationshipService relationshipService;
    private final RateLimitService rateLimitService;

    @Transactional
    public Notification createNotification(UUID senderId, CreateNotificationRequest request) {
        validateCreateRequest(request);

        UUID recipientId = request.recipientId() != null ? request.recipientId() : senderId;
        if (!recipientId.equals(senderId)
                && !relationshipService.areUsersLinked(senderId, recipientId)) {
            throw new AccessDeniedException("Notifications can only be sent to linked Pals");
        }

        rateLimitService.checkAndRecord(
                senderId,
                "NOTIFICATION_CREATE",
                MAX_NOTIFICATIONS_PER_HOUR,
                Duration.ofHours(1),
                "You can schedule at most 10 reminders per hour");

        Notification notification = new Notification();
        notification.setSenderId(senderId);
        notification.setRecipientId(recipientId);
        notification.setTitle(request.title().trim());
        notification.setBody(request.body());
        notification.setIcon(NotificationIcon.fromApiValue(request.icon()));
        notification.setScheduledTime(request.scheduledTime());
        notification.setStatus(NotificationStatus.PENDING);
        return notificationRepository.save(notification);
    }

    public Notification findAccessibleById(UUID id, UUID userId) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Notification not found"));
        if (!userId.equals(notification.getSenderId())
                && !userId.equals(notification.getRecipientId())) {
            throw new EntityNotFoundException("Notification not found");
        }
        return notification;
    }

    public Page<Notification> getSentNotifications(UUID senderId, Pageable pageable) {
        return notificationRepository.findBySenderIdOrderByCreatedAtDesc(senderId, pageable);
    }

    public Page<Notification> getReceivedNotifications(UUID recipientId, Pageable pageable) {
        return notificationRepository.findReceivedNotifications(recipientId, pageable);
    }

    public Page<Notification> getPendingNotifications(Instant now, Pageable pageable) {
        return notificationRepository.findPendingNotifications(now, pageable);
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public Optional<Notification> lockDueNotification(UUID id, Instant now) {
        return notificationRepository.findByIdForUpdate(id)
                .filter(notification -> notification.getStatus() == NotificationStatus.PENDING)
                .filter(notification -> isDue(notification, now));
    }

    @Transactional
    public Notification markAsSent(UUID id) {
        Notification notification = findForUpdate(id);
        requireStatus(notification, NotificationStatus.PENDING, "Notification is not pending");
        notification.setStatus(NotificationStatus.SENT);
        notification.setSentAt(Instant.now());
        notification.setNextAttemptAt(null);
        notification.setFailureReason(null);
        return notificationRepository.save(notification);
    }

    @Transactional
    public Notification markAsDelivered(UUID id) {
        Notification notification = findForUpdate(id);
        if (notification.getStatus() == NotificationStatus.DELIVERED
                || notification.getStatus() == NotificationStatus.VIEWED) {
            return notification;
        }
        requireStatus(notification, NotificationStatus.SENT,
                "Only sent notifications can be marked delivered");
        notification.setStatus(NotificationStatus.DELIVERED);
        notification.setDeliveredAt(Instant.now());
        return notificationRepository.save(notification);
    }

    @Transactional
    public Notification markAsViewed(UUID id, UUID userId) {
        Notification notification = findForUpdate(id);
        if (!userId.equals(notification.getRecipientId())) {
            throw new EntityNotFoundException("Notification not found");
        }
        if (notification.getStatus() == NotificationStatus.VIEWED) {
            return notification;
        }
        if (notification.getStatus() != NotificationStatus.SENT
                && notification.getStatus() != NotificationStatus.DELIVERED) {
            throw new IllegalStateException("Only sent notifications can be marked viewed");
        }
        notification.setStatus(NotificationStatus.VIEWED);
        notification.setViewedAt(Instant.now());
        return notificationRepository.save(notification);
    }

    @Transactional
    public Notification markAsFailed(UUID id, String reason) {
        Notification notification = findForUpdate(id);
        requireStatus(notification, NotificationStatus.PENDING, "Notification is not pending");
        notification.setStatus(NotificationStatus.FAILED);
        notification.setFailureReason(reason);
        notification.setNextAttemptAt(null);
        return notificationRepository.save(notification);
    }

    @Transactional
    public Notification markForRetry(UUID id, String reason) {
        Notification notification = findForUpdate(id);
        requireStatus(notification, NotificationStatus.PENDING, "Notification is not pending");
        if (notification.getRetryCount() >= 1) {
            throw new IllegalStateException("Notification retry has already been used");
        }
        notification.setRetryCount(notification.getRetryCount() + 1);
        notification.setNextAttemptAt(Instant.now().plusSeconds(RETRY_DELAY_SECONDS));
        notification.setFailureReason(reason);
        return notificationRepository.save(notification);
    }

    @Transactional
    public Notification cancelNotification(UUID id, UUID senderId) {
        Notification notification = findForUpdate(id);
        if (!senderId.equals(notification.getSenderId())) {
            throw new EntityNotFoundException("Notification not found");
        }
        requireStatus(notification, NotificationStatus.PENDING,
                "Can only cancel pending notifications");
        notification.setStatus(NotificationStatus.CANCELLED);
        notification.setNextAttemptAt(null);
        return notificationRepository.save(notification);
    }

    private Notification findForUpdate(UUID id) {
        return notificationRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new EntityNotFoundException("Notification not found"));
    }

    private void validateCreateRequest(CreateNotificationRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Notification request is required");
        }
        if (request.title() == null || request.title().isBlank()) {
            throw new IllegalArgumentException("Title is required");
        }
        if (request.title().length() > MAX_TITLE_LENGTH) {
            throw new IllegalArgumentException("Title must be at most 100 characters");
        }
        if (request.body() != null && request.body().length() > MAX_BODY_LENGTH) {
            throw new IllegalArgumentException("Body must be at most 500 characters");
        }
        if (request.scheduledTime() == null || !request.scheduledTime().isAfter(Instant.now())) {
            throw new IllegalArgumentException("Scheduled time must be in the future");
        }
    }

    private void requireStatus(Notification notification, NotificationStatus expected, String message) {
        if (notification.getStatus() != expected) {
            throw new IllegalStateException(message);
        }
    }

    private boolean isDue(Notification notification, Instant now) {
        if (notification.getRetryCount() == 0) {
            return !notification.getScheduledTime().isAfter(now);
        }
        return notification.getNextAttemptAt() != null
                && !notification.getNextAttemptAt().isAfter(now);
    }
}
