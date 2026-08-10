package com.pushpal.push;

import com.pushpal.device.PushSubscription;
import com.pushpal.device.PushSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("pushDeliveryService")
@RequiredArgsConstructor
@Slf4j
public class PushService {

    private final NotificationProvider notificationProvider;
    private final PushSubscriptionRepository pushSubscriptionRepository;

    public AggregatedResult sendToAll(List<PushSubscription> subscriptions,
                                     NotificationProvider.NotificationPayload payload) {
        int successCount = 0;
        int failureCount = 0;
        String firstError = null;

        for (PushSubscription subscription : subscriptions) {
            var result = notificationProvider.send(subscription, payload);
            if (result.success()) {
                successCount++;
            } else {
                failureCount++;
                if (firstError == null) {
                    firstError = result.errorMessage();
                }
                if (result.subscriptionGone()) {
                    log.info("Removing stale push subscription {}", subscription.getId());
                    pushSubscriptionRepository.delete(subscription);
                }
            }
        }

        log.debug("Push results: {} success, {} failures", successCount, failureCount);
        return new AggregatedResult(successCount, failureCount, firstError);
    }

    public record AggregatedResult(int successCount, int failureCount, String firstError) {}
}
