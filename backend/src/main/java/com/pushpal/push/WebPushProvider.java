package com.pushpal.push;

import com.pushpal.device.PushSubscription;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import org.apache.http.HttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

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
            log.warn("Web Push is not configured; skipping notification to endpoint: {}", subscription.getEndpoint());
            return SendResult.failure("Web Push not configured");
        }
        try {
            String jsonPayload = gson.toJson(Map.of(
                    "title", payload.title(),
                    "body", payload.body(),
                    "data", payload.data()));

            Notification notification = new Notification(
                    subscription.getEndpoint(),
                    subscription.getP256dh(),
                    subscription.getAuthKey(),
                    jsonPayload.getBytes());

            HttpResponse response = pushService.send(notification);
            int status = response.getStatusLine().getStatusCode();

            if (status >= 200 && status < 300) {
                return SendResult.ok();
            }
            if (status == 404 || status == 410) {
                log.warn("Push endpoint gone (HTTP {}): {}", status, subscription.getEndpoint());
                return SendResult.subscriptionGone("Subscription no longer valid (HTTP " + status + ")");
            }
            log.warn("Push failed with HTTP {}: {}", status, subscription.getEndpoint());
            return SendResult.failure("Push service returned HTTP " + status);
        } catch (Exception e) {
            log.error("Failed to send push notification to endpoint: {}", subscription.getEndpoint(), e);
            return SendResult.failure(e.getMessage());
        }
    }
}
