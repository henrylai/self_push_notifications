package com.pushpal.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    @Transactional
    public Notification createNotification(UUID senderId, CreateNotificationRequest request) {
        Notification notification = new Notification();
        notification.setSenderId(senderId);
        notification.setRecipientId(
                request.recipientId() != null ? request.recipientId() : senderId);
        notification.setTitle(request.title());
        notification.setBody(request.body());
        notification.setScheduledTime(
                request.scheduledTime() != null ? request.scheduledTime() : Instant.now());
        notification.setStatus(NotificationStatus.PENDING);
        return notificationRepository.save(notification);
    }

    public Optional<Notification> findById(UUID id) {
        return notificationRepository.findById(id);
    }

    public List<Notification> getSentNotifications(UUID senderId) {
        return notificationRepository.findBySenderIdOrderByCreatedAtDesc(senderId);
    }

    public List<Notification> getReceivedNotifications(UUID recipientId) {
        return notificationRepository.findByRecipientIdOrderByScheduledTimeDesc(recipientId);
    }

    public Page<Notification> getPendingNotifications(Instant now, Pageable pageable) {
        return notificationRepository.findPendingNotifications(now, pageable);
    }

    @Transactional
    public Notification markAsSent(UUID id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found"));
        notification.setStatus(NotificationStatus.SENT);
        notification.setSentAt(Instant.now());
        return notificationRepository.save(notification);
    }

    @Transactional
    public Notification markAsDelivered(UUID id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found"));
        notification.setStatus(NotificationStatus.DELIVERED);
        notification.setDeliveredAt(Instant.now());
        return notificationRepository.save(notification);
    }

    @Transactional
    public Notification markAsViewed(UUID id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found"));
        notification.setStatus(NotificationStatus.VIEWED);
        notification.setViewedAt(Instant.now());
        return notificationRepository.save(notification);
    }

    @Transactional
    public Notification markAsFailed(UUID id, String reason) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found"));
        notification.setStatus(NotificationStatus.FAILED);
        notification.setFailureReason(reason);
        return notificationRepository.save(notification);
    }

    @Transactional
    public Notification cancelNotification(UUID id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found"));
        if (notification.getStatus() != NotificationStatus.PENDING) {
            throw new IllegalStateException("Can only cancel pending notifications");
        }
        notification.setStatus(NotificationStatus.CANCELLED);
        return notificationRepository.save(notification);
    }
}
