package com.pushpal.push;

import com.pushpal.device.PushSubscription;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
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
            return new SendResult(false, "Web Push not configured");
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

            pushService.send(notification);
            return new SendResult(true, null);
        } catch (Exception e) {
            log.error("Failed to send push notification to endpoint: {}", subscription.getEndpoint(), e);
            return new SendResult(false, e.getMessage());
        }
    }
}
