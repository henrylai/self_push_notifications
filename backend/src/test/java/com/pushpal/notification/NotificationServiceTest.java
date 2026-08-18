package com.pushpal.notification;

import com.pushpal.common.RateLimitExceededException;
import com.pushpal.common.RateLimitService;
import com.pushpal.relationship.RelationshipService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private RelationshipService relationshipService;

    @Mock
    private RateLimitService rateLimitService;

    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationService(
                notificationRepository, relationshipService, rateLimitService);
    }

    @Test
    void createsSelfNotificationWithOptionalBody() {
        UUID senderId = UUID.randomUUID();
        CreateNotificationRequest request = new CreateNotificationRequest(
                "  Check the oven  ", null, null, null, Instant.now().plusSeconds(300));
        when(notificationRepository.save(any(Notification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Notification result = notificationService.createNotification(senderId, request);

        assertThat(result.getSenderId()).isEqualTo(senderId);
        assertThat(result.getRecipientId()).isEqualTo(senderId);
        assertThat(result.getTitle()).isEqualTo("Check the oven");
        assertThat(result.getBody()).isNull();
        assertThat(result.getIcon()).isEqualTo(NotificationIcon.BELL);
    }

    @Test
    void storesTheSelectedNotificationIcon() {
        UUID senderId = UUID.randomUUID();
        CreateNotificationRequest request = new CreateNotificationRequest(
                "Celebrate", "", "gift", null, Instant.now().plusSeconds(300));
        when(notificationRepository.save(any(Notification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Notification result = notificationService.createNotification(senderId, request);

        assertThat(result.getIcon()).isEqualTo(NotificationIcon.GIFT);
    }

    @Test
    void rejectsUnlinkedRecipient() {
        UUID senderId = UUID.randomUUID();
        UUID recipientId = UUID.randomUUID();
        CreateNotificationRequest request = validRequest(recipientId);
        when(relationshipService.areUsersLinked(senderId, recipientId)).thenReturn(false);

        assertThatThrownBy(() -> notificationService.createNotification(senderId, request))
                .isInstanceOf(AccessDeniedException.class);
        verify(notificationRepository, never()).save(any());
    }

    @Test
    void rejectsEleventhNotificationWithinHour() {
        UUID senderId = UUID.randomUUID();
        org.mockito.Mockito.doThrow(new RateLimitExceededException(
                        "You can schedule at most 10 reminders per hour"))
                .when(rateLimitService)
                .checkAndRecord(
                        org.mockito.ArgumentMatchers.eq(senderId),
                        org.mockito.ArgumentMatchers.eq("NOTIFICATION_CREATE"),
                        org.mockito.ArgumentMatchers.eq(10),
                        any(),
                        any());

        assertThatThrownBy(() -> notificationService.createNotification(senderId, validRequest(null)))
                .isInstanceOf(RateLimitExceededException.class)
                .hasMessageContaining("10 reminders");
    }

    @Test
    void hidesNotificationFromUnrelatedUser() {
        Notification notification = notification(
                NotificationStatus.PENDING, UUID.randomUUID(), UUID.randomUUID());
        when(notificationRepository.findById(notification.getId()))
                .thenReturn(Optional.of(notification));

        assertThatThrownBy(() -> notificationService.findAccessibleById(
                notification.getId(), UUID.randomUUID()))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void preventsNonSenderFromCancellingNotification() {
        Notification notification = notification(
                NotificationStatus.PENDING, UUID.randomUUID(), UUID.randomUUID());
        when(notificationRepository.findByIdForUpdate(notification.getId()))
                .thenReturn(Optional.of(notification));

        assertThatThrownBy(() -> notificationService.cancelNotification(
                notification.getId(), notification.getRecipientId()))
                .isInstanceOf(EntityNotFoundException.class);
        verify(notificationRepository, never()).save(any());
    }

    @Test
    void deliveredCallbackCannotRegressViewedNotification() {
        Notification notification = notification(
                NotificationStatus.VIEWED, UUID.randomUUID(), UUID.randomUUID());
        when(notificationRepository.findByIdForUpdate(notification.getId()))
                .thenReturn(Optional.of(notification));

        Notification result = notificationService.markAsDelivered(notification.getId());

        assertThat(result.getStatus()).isEqualTo(NotificationStatus.VIEWED);
        verify(notificationRepository, never()).save(any());
    }

    @Test
    void pendingNotificationCannotBeMarkedViewed() {
        Notification notification = notification(
                NotificationStatus.PENDING, UUID.randomUUID(), UUID.randomUUID());
        when(notificationRepository.findByIdForUpdate(notification.getId()))
                .thenReturn(Optional.of(notification));

        assertThatThrownBy(() -> notificationService.markAsViewed(
                notification.getId(), notification.getRecipientId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void schedulesSingleRetryInTheFuture() {
        Notification notification = notification(
                NotificationStatus.PENDING, UUID.randomUUID(), UUID.randomUUID());
        when(notificationRepository.findByIdForUpdate(notification.getId()))
                .thenReturn(Optional.of(notification));
        when(notificationRepository.save(notification)).thenReturn(notification);

        Instant before = Instant.now().plusSeconds(59);
        Notification result = notificationService.markForRetry(notification.getId(), "failed");

        assertThat(result.getRetryCount()).isEqualTo(1);
        assertThat(result.getNextAttemptAt()).isAfter(before);
    }

    @Test
    void lockedNotificationIsSkippedAfterAnotherWorkerSendsIt() {
        Notification notification = notification(
                NotificationStatus.SENT, UUID.randomUUID(), UUID.randomUUID());
        when(notificationRepository.findByIdForUpdate(notification.getId()))
                .thenReturn(Optional.of(notification));

        Optional<Notification> result = notificationService.lockDueNotification(
                notification.getId(), Instant.now());

        assertThat(result).isEmpty();
    }

    private CreateNotificationRequest validRequest(UUID recipientId) {
        return new CreateNotificationRequest(
                "Reminder", "Details", "heart", recipientId, Instant.now().plusSeconds(300));
    }

    private Notification notification(NotificationStatus status, UUID senderId, UUID recipientId) {
        Notification notification = new Notification();
        notification.setId(UUID.randomUUID());
        notification.setSenderId(senderId);
        notification.setRecipientId(recipientId);
        notification.setTitle("Reminder");
        notification.setScheduledTime(Instant.now().plusSeconds(300));
        notification.setStatus(status);
        return notification;
    }
}
