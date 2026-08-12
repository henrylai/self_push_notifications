package com.pushpal.notification;

import java.util.Arrays;

public enum NotificationIcon {
    BELL,
    HEART,
    STAR,
    CHECK,
    CALENDAR,
    GIFT;

    public static NotificationIcon fromApiValue(String value) {
        if (value == null || value.isBlank()) {
            return BELL;
        }
        return Arrays.stream(values())
                .filter(icon -> icon.apiValue().equals(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported notification icon"));
    }

    public String apiValue() {
        return name().toLowerCase();
    }
}
