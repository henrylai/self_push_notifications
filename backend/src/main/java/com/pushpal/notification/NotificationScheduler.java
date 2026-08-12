package com.pushpal.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@ConditionalOnProperty(name = "app.scheduler.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class NotificationScheduler {

    private final NotificationService notificationService;
    private final NotificationDeliveryService notificationDeliveryService;

    @Scheduled(fixedDelayString = "${app.scheduler.interval-ms:10000}")
    public void processPendingNotifications() {
        Page<Notification> pending = notificationService.getPendingNotifications(
                Instant.now(), PageRequest.of(0, 50));

        log.debug("Processing {} pending notifications", pending.getContent().size());
        for (Notification notification : pending.getContent()) {
            try {
                notificationDeliveryService.process(notification.getId());
            } catch (Exception e) {
                log.error("Error processing notification {}", notification.getId(), e);
            }
        }
    }
}
