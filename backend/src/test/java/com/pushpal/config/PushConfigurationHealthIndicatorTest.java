package com.pushpal.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Status;

import static org.assertj.core.api.Assertions.assertThat;

class PushConfigurationHealthIndicatorTest {

    @Test
    void reportsDownWhenPushConfigurationIsIncomplete() {
        var indicator = new PushConfigurationHealthIndicator("", "private", "http://api.example.com");

        var health = indicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        assertThat(health.getDetails().get("missingOrInvalidConfiguration").toString())
                .contains("VAPID_PUBLIC_KEY", "API_BASE_URL");
    }

    @Test
    void reportsUpForCompleteProductionConfiguration() {
        var indicator = new PushConfigurationHealthIndicator(
                "public", "private", "https://pushpal-api.example.com");

        assertThat(indicator.health().getStatus()).isEqualTo(Status.UP);
    }
}
