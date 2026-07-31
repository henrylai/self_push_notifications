package com.pushpal.notification;

import com.pushpal.device.PushSubscription;
import com.pushpal.device.PushSubscriptionRepository;
import com.pushpal.push.PushService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationScheduler {

    private final NotificationService notificationService;
    private final PushSubscriptionRepository pushSubscriptionRepository;
    private final PushService pushService;

    @Scheduled(fixedDelayString = "${app.scheduler.interval-ms:30000}")
    public void processPendingNotifications() {
        if (pushService == null) {
            log.debug("PushService not configured, skipping scheduler run");
            return;
        }

        Page<Notification> pending = notificationService.getPendingNotifications(
                Instant.now(), PageRequest.of(0, 50));

        log.debug("Processing {} pending notifications", pending.getContent().size());

        for (Notification notification : pending.getContent()) {
            try {
                List<PushSubscription> subscriptions =
                        pushSubscriptionRepository.findByUserId(notification.getRecipientId());

                if (subscriptions.isEmpty()) {
                    log.warn("No push subscriptions for user {}, marking as failed",
                            notification.getRecipientId());
                    notificationService.markAsFailed(notification.getId(),
                            "No push subscriptions registered");
                    continue;
                }

                var payload = new com.pushpal.push.NotificationProvider.NotificationPayload(
                        notification.getTitle(),
                        notification.getBody(),
                        java.util.Map.of("notificationId", notification.getId().toString()));

                var result = pushService.sendToAll(subscriptions, payload);

                if (result.successCount() > 0) {
                    notificationService.markAsSent(notification.getId());
                    log.info("Notification {} sent to {} devices",
                            notification.getId(), result.successCount());
                } else {
                    notificationService.markAsFailed(notification.getId(),
                            result.firstError());
                    log.warn("Notification {} failed: {}", notification.getId(), result.firstError());
                }
            } catch (Exception e) {
                log.error("Error processing notification {}", notification.getId(), e);
                notificationService.markAsFailed(notification.getId(), e.getMessage());
            }
        }
    }
}
