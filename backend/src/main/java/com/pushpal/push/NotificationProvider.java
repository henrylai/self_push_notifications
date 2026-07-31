package com.pushpal.push;

import com.pushpal.device.PushSubscription;

import java.util.Map;

public interface NotificationProvider {

    SendResult send(PushSubscription subscription, NotificationPayload payload);

    record NotificationPayload(String title, String body, Map<String, String> data) {}

    record SendResult(boolean success, String errorMessage) {}
}
