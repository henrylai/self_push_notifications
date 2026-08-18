package com.pushpal.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Component("pushConfiguration")
public class PushConfigurationHealthIndicator implements HealthIndicator {

    private final String vapidPublicKey;
    private final String vapidPrivateKey;
    private final String apiBaseUrl;

    public PushConfigurationHealthIndicator(
            @Value("${app.vapid.public-key:}") String vapidPublicKey,
            @Value("${app.vapid.private-key:}") String vapidPrivateKey,
            @Value("${app.api-base-url:}") String apiBaseUrl) {
        this.vapidPublicKey = vapidPublicKey;
        this.vapidPrivateKey = vapidPrivateKey;
        this.apiBaseUrl = apiBaseUrl;
    }

    @Override
    public Health health() {
        List<String> missing = new ArrayList<>();
        if (vapidPublicKey == null || vapidPublicKey.isBlank()) {
            missing.add("VAPID_PUBLIC_KEY");
        }
        if (vapidPrivateKey == null || vapidPrivateKey.isBlank()) {
            missing.add("VAPID_PRIVATE_KEY");
        }
        if (!isValidApiBaseUrl(apiBaseUrl)) {
            missing.add("API_BASE_URL");
        }
        if (!missing.isEmpty()) {
            return Health.down()
                    .withDetail("missingOrInvalidConfiguration", missing)
                    .build();
        }
        return Health.up().build();
    }

    private boolean isValidApiBaseUrl(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        try {
            URI uri = URI.create(value);
            return "https".equalsIgnoreCase(uri.getScheme()) && uri.getHost() != null;
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }
}
