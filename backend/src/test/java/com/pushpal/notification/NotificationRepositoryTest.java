package com.pushpal.notification;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import jakarta.persistence.EntityManager;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class NotificationRepositoryTest {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void retryWaitsUntilNextAttemptEvenWhenScheduledTimeIsPast() {
        Notification notification = retryNotification(Instant.now().plusSeconds(60));
        notificationRepository.saveAndFlush(notification);

        var due = notificationRepository.findPendingNotifications(
                Instant.now(), PageRequest.of(0, 50));

        assertThat(due.getContent()).isEmpty();
    }

    @Test
    void retryBecomesEligibleAtNextAttempt() {
        Notification notification = retryNotification(Instant.now().minusSeconds(1));
        notificationRepository.saveAndFlush(notification);

        var due = notificationRepository.findPendingNotifications(
                Instant.now(), PageRequest.of(0, 50));

        assertThat(due.getContent()).extracting(Notification::getId)
                .containsExactly(notification.getId());
    }

    @Test
    void cancelsOnlyPendingRemindersBetweenRemovedPals() {
        UUID firstUserId = UUID.randomUUID();
        UUID secondUserId = UUID.randomUUID();
        Notification forwardPending = notification(firstUserId, secondUserId, NotificationStatus.PENDING);
        Notification reversePending = notification(secondUserId, firstUserId, NotificationStatus.PENDING);
        Notification alreadySent = notification(firstUserId, secondUserId, NotificationStatus.SENT);
        Notification unrelated = notification(firstUserId, UUID.randomUUID(), NotificationStatus.PENDING);
        notificationRepository.saveAllAndFlush(List.of(
                forwardPending, reversePending, alreadySent, unrelated));

        int cancelled = notificationRepository.cancelPendingNotificationsBetweenUsers(
                firstUserId, secondUserId);
        entityManager.clear();

        assertThat(cancelled).isEqualTo(2);
        assertThat(notificationRepository.findById(forwardPending.getId()).orElseThrow().getStatus())
                .isEqualTo(NotificationStatus.CANCELLED);
        assertThat(notificationRepository.findById(reversePending.getId()).orElseThrow().getStatus())
                .isEqualTo(NotificationStatus.CANCELLED);
        assertThat(notificationRepository.findById(alreadySent.getId()).orElseThrow().getStatus())
                .isEqualTo(NotificationStatus.SENT);
        assertThat(notificationRepository.findById(unrelated.getId()).orElseThrow().getStatus())
                .isEqualTo(NotificationStatus.PENDING);
    }

    private Notification retryNotification(Instant nextAttemptAt) {
        Notification notification = new Notification();
        notification.setSenderId(UUID.randomUUID());
        notification.setRecipientId(UUID.randomUUID());
        notification.setTitle("Retry me");
        notification.setScheduledTime(Instant.now().minusSeconds(300));
        notification.setStatus(NotificationStatus.PENDING);
        notification.setRetryCount(1);
        notification.setNextAttemptAt(nextAttemptAt);
        return notification;
    }

    private Notification notification(UUID senderId, UUID recipientId, NotificationStatus status) {
        Notification notification = new Notification();
        notification.setSenderId(senderId);
        notification.setRecipientId(recipientId);
        notification.setTitle("Reminder");
        notification.setScheduledTime(Instant.now().plusSeconds(300));
        notification.setStatus(status);
        return notification;
    }
}
