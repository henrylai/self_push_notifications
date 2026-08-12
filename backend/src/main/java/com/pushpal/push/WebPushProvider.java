package com.pushpal.push;

import com.pushpal.device.PushSubscription;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import nl.martijndwars.webpush.Urgency;
import org.apache.http.HttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class WebPushProvider implements NotificationProvider {

    private final PushService pushService;
    private final Gson gson = new Gson();

    public WebPushProvider(@Autowired(required = false) @Qualifier("pushService") PushService pushService) {
        this.pushService = pushService;
    }

    @Override
    public SendResult send(PushSubscription subscription, NotificationPayload payload) {
        if (pushService == null) {
            log.warn("Web Push is not configured; skipping subscription {}", subscription.getId());
            return SendResult.failure("Web Push not configured");
        }
        try {
            Map<String, Object> pushPayload = new HashMap<>();
            pushPayload.put("title", payload.title());
            pushPayload.put("body", payload.body());
            pushPayload.put("data", payload.data());
            String jsonPayload = gson.toJson(pushPayload);

            // Reminder notifications are time-sensitive. Explicitly requesting high urgency lets
            // a push service and the operating system prioritize them over background updates.
            Notification notification = new Notification(
                    subscription.getEndpoint(),
                    subscription.getP256dh(),
                    subscription.getAuthKey(),
                    jsonPayload,
                    Urgency.HIGH);

            HttpResponse response = pushService.send(notification);
            int status = response.getStatusLine().getStatusCode();

            if (status >= 200 && status < 300) {
                return SendResult.ok();
            }
            if (status == 404 || status == 410) {
                log.warn("Push subscription {} is gone (HTTP {})", subscription.getId(), status);
                return SendResult.subscriptionGone("Subscription no longer valid (HTTP " + status + ")");
            }
            log.warn("Push subscription {} failed with HTTP {}", subscription.getId(), status);
            return SendResult.failure("Push service returned HTTP " + status);
        } catch (Exception e) {
            log.error("Failed to send push notification for subscription {}", subscription.getId(), e);
            return SendResult.failure("Push delivery failed");
        }
    }
}
