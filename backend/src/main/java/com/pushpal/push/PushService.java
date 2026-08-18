package com.pushpal.push;

import com.pushpal.device.PushSubscription;
import com.pushpal.device.PushSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service("pushDeliveryService")
@RequiredArgsConstructor
@Slf4j
public class PushService {

    private final NotificationProvider notificationProvider;
    private final PushSubscriptionRepository pushSubscriptionRepository;

    public AggregatedResult sendToAll(List<PushSubscription> subscriptions,
                                     NotificationProvider.NotificationPayload payload) {
        List<SubscriptionResult> results = new java.util.ArrayList<>();

        for (PushSubscription subscription : subscriptions) {
            var result = notificationProvider.send(subscription, payload);
            results.add(new SubscriptionResult(
                    subscription.getId(), result.success(), result.subscriptionGone(), result.errorMessage()));
            if (!result.success()) {
                if (result.subscriptionGone()) {
                    log.info("Revoking stale push subscription {}", subscription.getId());
                    subscription.setRevoked(true);
                    subscription.setRevocationReason("EXPIRED");
                    pushSubscriptionRepository.save(subscription);
                }
            }
        }

        AggregatedResult aggregated = new AggregatedResult(results);
        log.debug("Push results: {} success, {} failures",
                aggregated.successCount(), aggregated.failureCount());
        return aggregated;
    }

    public record SubscriptionResult(
            UUID subscriptionId,
            boolean success,
            boolean subscriptionGone,
            String errorMessage) {}

    public record AggregatedResult(List<SubscriptionResult> results) {

        public int successCount() {
            return (int) results.stream().filter(SubscriptionResult::success).count();
        }

        public int failureCount() {
            return results.size() - successCount();
        }

        public String firstError() {
            return results.stream()
                    .filter(result -> !result.success())
                    .map(SubscriptionResult::errorMessage)
                    .filter(error -> error != null && !error.isBlank())
                    .findFirst()
                    .orElse(null);
        }
    }
}
