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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationDeliveryService {

    private final NotificationService notificationService;
    private final PushSubscriptionRepository pushSubscriptionRepository;
    private final PushService pushService;
    private final DeliveryTokenProvider deliveryTokenProvider;
    private final NotificationDeliveryRepository notificationDeliveryRepository;

    private static final long RETRY_DELAY_SECONDS = 60;

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

        Instant now = Instant.now();
        List<PushSubscription> subscriptions = pushSubscriptionRepository
                .findByUserIdAndRevokedFalse(notification.getRecipientId());
        List<NotificationDelivery> deliveries = new ArrayList<>(notificationDeliveryRepository
                .findByNotificationId(notification.getId()));
        addDeliveryRecordsWhenEligible(notification, subscriptions, deliveries);

        Map<UUID, PushSubscription> subscriptionsById = subscriptions.stream()
                .collect(Collectors.toMap(PushSubscription::getId, Function.identity()));
        List<NotificationDelivery> dueDeliveries = deliveries.stream()
                .filter(delivery -> delivery.getStatus() == NotificationDeliveryStatus.PENDING)
                .filter(delivery -> delivery.getNextAttemptAt() == null
                        || !delivery.getNextAttemptAt().isAfter(now))
                .toList();

        List<PushSubscription> targets = new ArrayList<>();
        for (NotificationDelivery delivery : dueDeliveries) {
            PushSubscription subscription = subscriptionsById.get(delivery.getSubscriptionId());
            if (subscription == null) {
                delivery.setStatus(NotificationDeliveryStatus.INVALID);
                delivery.setFailureReason("Subscription no longer active");
            } else {
                targets.add(subscription);
            }
        }

        if (targets.isEmpty()) {
            notificationDeliveryRepository.saveAll(deliveries);
            finalizeOrRetry(notification, deliveries, "No push subscriptions registered");
            return;
        }

        var payload = new NotificationProvider.NotificationPayload(
                notification.getTitle(),
                notification.getBody(),
                buildPayloadData(notification));
        var result = pushService.sendToAll(targets, payload);

        Map<UUID, PushService.SubscriptionResult> resultsBySubscription = result.results().stream()
                .collect(Collectors.toMap(PushService.SubscriptionResult::subscriptionId, Function.identity()));
        for (NotificationDelivery delivery : dueDeliveries) {
            PushService.SubscriptionResult deliveryResult = resultsBySubscription.get(delivery.getSubscriptionId());
            if (deliveryResult == null) {
                continue;
            }
            delivery.setAttemptCount(delivery.getAttemptCount() + 1);
            delivery.setFailureReason(deliveryResult.errorMessage());
            if (deliveryResult.success()) {
                delivery.setStatus(NotificationDeliveryStatus.SENT);
                delivery.setSentAt(now);
                delivery.setNextAttemptAt(null);
            } else if (deliveryResult.subscriptionGone()) {
                delivery.setStatus(NotificationDeliveryStatus.INVALID);
                delivery.setNextAttemptAt(null);
            } else if (notification.getRetryCount() < 1) {
                delivery.setNextAttemptAt(now.plusSeconds(RETRY_DELAY_SECONDS));
            } else {
                delivery.setStatus(NotificationDeliveryStatus.FAILED);
                delivery.setNextAttemptAt(null);
            }
        }
        notificationDeliveryRepository.saveAll(deliveries);

        String failureReason = result.firstError();
        finalizeOrRetry(notification, deliveries,
                failureReason == null || failureReason.isBlank() ? "Push delivery failed" : failureReason);
    }

    private void addDeliveryRecordsWhenEligible(Notification notification,
                                                List<PushSubscription> subscriptions,
                                                List<NotificationDelivery> deliveries) {
        boolean hasActionableDelivery = deliveries.stream()
                .anyMatch(delivery -> delivery.getStatus() != NotificationDeliveryStatus.INVALID);
        if (hasActionableDelivery) {
            return;
        }
        var existingSubscriptionIds = deliveries.stream()
                .map(NotificationDelivery::getSubscriptionId)
                .collect(Collectors.toSet());
        for (PushSubscription subscription : subscriptions) {
            if (existingSubscriptionIds.contains(subscription.getId())) {
                deliveries.stream()
                        .filter(delivery -> subscription.getId().equals(delivery.getSubscriptionId()))
                        .filter(delivery -> delivery.getStatus() == NotificationDeliveryStatus.INVALID)
                        .findFirst()
                        .ifPresent(delivery -> {
                            delivery.setStatus(NotificationDeliveryStatus.PENDING);
                            delivery.setFailureReason(null);
                            delivery.setNextAttemptAt(null);
                        });
                continue;
            }
            NotificationDelivery delivery = new NotificationDelivery();
            delivery.setNotificationId(notification.getId());
            delivery.setSubscriptionId(subscription.getId());
            deliveries.add(notificationDeliveryRepository.save(delivery));
        }
    }

    private void finalizeOrRetry(Notification notification,
                                 List<NotificationDelivery> deliveries,
                                 String reason) {
        boolean hasPending = deliveries.stream()
                .anyMatch(delivery -> delivery.getStatus() == NotificationDeliveryStatus.PENDING);
        long sentCount = deliveries.stream()
                .filter(delivery -> delivery.getStatus() == NotificationDeliveryStatus.SENT)
                .count();
        if ((hasPending || sentCount == 0) && notification.getRetryCount() < 1) {
            notificationService.markForRetry(notification.getId(), reason);
            log.warn("Notification {} failed; retry scheduled", notification.getId());
            return;
        }

        boolean hasFailed = deliveries.stream()
                .anyMatch(delivery -> delivery.getStatus() == NotificationDeliveryStatus.FAILED);
        if (hasFailed || sentCount == 0) {
            notificationService.markAsFailed(notification.getId(), reason);
            log.warn("Notification {} failed after retry", notification.getId());
            return;
        }
        notificationService.markAsSent(notification.getId());
        log.info("Notification {} sent to {} devices", notification.getId(), sentCount);
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
