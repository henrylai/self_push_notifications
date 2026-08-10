package com.pushpal.notification;

import com.pushpal.auth.JwtTokenProvider;
import com.pushpal.device.PushSubscription;
import com.pushpal.device.PushSubscriptionRepository;
import com.pushpal.push.NotificationProvider;
import com.pushpal.push.PushService;
import com.pushpal.user.User;
import com.pushpal.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationScheduler {

    private final NotificationService notificationService;
    private final PushSubscriptionRepository pushSubscriptionRepository;
    private final PushService pushService;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    @Value("${app.api-base-url:}")
    private String apiBaseUrl;

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

                var payload = new NotificationProvider.NotificationPayload(
                        notification.getTitle(),
                        notification.getBody(),
                        buildPayloadData(notification));

                var result = pushService.sendToAll(subscriptions, payload);

                if (result.successCount() > 0) {
                    notificationService.markAsSent(notification.getId());
                    log.info("Notification {} sent to {} devices",
                            notification.getId(), result.successCount());
                } else if (notification.getRetryCount() < 1) {
                    notificationService.markForRetry(notification.getId(), result.firstError());
                    log.warn("Notification {} failed, will retry in 60s: {}",
                            notification.getId(), result.firstError());
                } else {
                    notificationService.markAsFailed(notification.getId(),
                            result.firstError());
                    log.warn("Notification {} failed after retry: {}", notification.getId(),
                            result.firstError());
                }
            } catch (Exception e) {
                log.error("Error processing notification {}", notification.getId(), e);
                notificationService.markAsFailed(notification.getId(), e.getMessage());
            }
        }
    }

    private Map<String, String> buildPayloadData(Notification notification) {
        Map<String, String> data = new HashMap<>();
        data.put("notificationId", notification.getId().toString());
        if (apiBaseUrl == null || apiBaseUrl.isBlank()) {
            return data;
        }
        data.put("apiUrl", apiBaseUrl);
        userRepository.findById(notification.getRecipientId())
                .map(jwtTokenProvider::generateToken)
                .ifPresent(token -> data.put("token", token));
        return data;
    }
}
