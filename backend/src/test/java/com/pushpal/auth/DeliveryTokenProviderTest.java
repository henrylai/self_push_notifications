package com.pushpal.auth;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DeliveryTokenProviderTest {

    private static final String SECRET = "test-secret-that-is-long-enough-for-hs256-signing";

    private final DeliveryTokenProvider provider = new DeliveryTokenProvider(SECRET, 10);

    @Test
    void tokenIsScopedToNotification() {
        UUID notificationId = UUID.randomUUID();
        String token = provider.generateToken(notificationId, UUID.randomUUID());

        assertThat(provider.validates(token, notificationId)).isTrue();
        assertThat(provider.validates(token, UUID.randomUUID())).isFalse();
    }

    @Test
    void rejectsMalformedOrMissingToken() {
        UUID notificationId = UUID.randomUUID();

        assertThat(provider.validates(null, notificationId)).isFalse();
        assertThat(provider.validates("not-a-token", notificationId)).isFalse();
    }
}
