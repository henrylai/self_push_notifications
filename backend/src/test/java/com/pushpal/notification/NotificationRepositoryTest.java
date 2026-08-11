package com.pushpal.notification;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class NotificationRepositoryTest {

    @Autowired
    private NotificationRepository notificationRepository;

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
}
