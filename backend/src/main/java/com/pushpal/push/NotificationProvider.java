package com.pushpal.push;

import com.pushpal.device.PushSubscription;

import java.util.Map;

public interface NotificationProvider {

    SendResult send(PushSubscription subscription, NotificationPayload payload);

    record NotificationPayload(String title, String body, Map<String, String> data) {}

    record SendResult(boolean success, String errorMessage, boolean subscriptionGone) {

        public static SendResult ok() {
            return new SendResult(true, null, false);
        }

        public static SendResult failure(String errorMessage) {
            return new SendResult(false, errorMessage, false);
        }

        public static SendResult subscriptionGone(String errorMessage) {
            return new SendResult(false, errorMessage, true);
        }
    }
}
