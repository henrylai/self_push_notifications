package com.pushpal.notification;

import com.pushpal.auth.DeliveryTokenProvider;
import com.pushpal.device.PushSubscription;
import com.pushpal.device.PushSubscriptionRepository;
import com.pushpal.push.NotificationProvider;
import com.pushpal.push.PushService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationDeliveryService {

    private final NotificationService notificationService;
    private final PushSubscriptionRepository pushSubscriptionRepository;
    private final PushService pushService;
    private final DeliveryTokenProvider deliveryTokenProvider;

    @Value("${app.api-base-url:}")
    private String apiBaseUrl;

    @Transactional
    public void process(UUID notificationId) {
        Notification notification = notificationService.lockDueNotification(
                        notificationId, Instant.now())
                .orElse(null);
        if (notification == null) {
            return;
        }

        List<PushSubscription> subscriptions =
                pushSubscriptionRepository.findByUserId(notification.getRecipientId());
        if (subscriptions.isEmpty()) {
            handleFailure(notification, "No push subscriptions registered");
            return;
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
        } else {
            String failureReason = result.firstError();
            handleFailure(notification, failureReason == null || failureReason.isBlank()
                    ? "Push delivery failed"
                    : failureReason);
        }
    }

    private void handleFailure(Notification notification, String reason) {
        if (notification.getRetryCount() < 1) {
            notificationService.markForRetry(notification.getId(), reason);
            log.warn("Notification {} failed; retry scheduled", notification.getId());
        } else {
            notificationService.markAsFailed(notification.getId(), reason);
            log.warn("Notification {} failed after retry", notification.getId());
        }
    }

    private Map<String, String> buildPayloadData(Notification notification) {
        Map<String, String> data = new HashMap<>();
        data.put("notificationId", notification.getId().toString());
        data.put("icon", notification.getIcon().apiValue());
        if (apiBaseUrl == null || apiBaseUrl.isBlank()) {
            return data;
        }
        data.put("apiUrl", apiBaseUrl);
        data.put("deliveryToken", deliveryTokenProvider.generateToken(
                notification.getId(), notification.getRecipientId()));
        return data;
    }
}
