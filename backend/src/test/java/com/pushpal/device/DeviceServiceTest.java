package com.pushpal.device;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeviceServiceTest {

    @Mock
    private PushSubscriptionRepository pushSubscriptionRepository;

    private DeviceService deviceService;

    @BeforeEach
    void setUp() {
        deviceService = new DeviceService(pushSubscriptionRepository);
    }

    @Test
    void userCannotRemoveAnotherUsersDevice() {
        PushSubscription subscription = new PushSubscription();
        subscription.setId(UUID.randomUUID());
        subscription.setUserId(UUID.randomUUID());
        when(pushSubscriptionRepository.findById(subscription.getId()))
                .thenReturn(Optional.of(subscription));

        assertThatThrownBy(() -> deviceService.removeSubscription(
                subscription.getId(), UUID.randomUUID()))
                .isInstanceOf(EntityNotFoundException.class);
        verify(pushSubscriptionRepository, never()).delete(subscription);
    }

    @Test
    void rejectsNonHttpsPushEndpoint() {
        assertThatThrownBy(() -> deviceService.registerSubscription(
                UUID.randomUUID(),
                "http://169.254.169.254/latest/meta-data",
                "public-key",
                "auth-key",
                "browser",
                false))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("HTTPS");
    }

    @Test
    void rejectsHttpsEndpointOutsideKnownPushProviders() {
        assertThatThrownBy(() -> deviceService.registerSubscription(
                UUID.randomUUID(),
                "https://example.com/capture-push-payloads",
                "public-key",
                "auth-key",
                "browser",
                false))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("HTTPS");
        verify(pushSubscriptionRepository, never()).save(any());
    }

    @Test
    void registersKnownBrowserPushEndpoint() {
        UUID userId = UUID.randomUUID();
        String endpoint = "https://fcm.googleapis.com/fcm/send/subscription";
        when(pushSubscriptionRepository.findByEndpoint(endpoint)).thenReturn(Optional.empty());
        when(pushSubscriptionRepository.save(any(PushSubscription.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PushSubscription result = deviceService.registerSubscription(
                userId, endpoint, "public-key", "auth-key", "browser", false);

        assertThat(result.getUserId()).isEqualTo(userId);
        assertThat(result.getEndpoint()).isEqualTo(endpoint);
        assertThat(result.isRevoked()).isFalse();
    }

    @Test
    void removedDeviceCannotBeSilentlyReactivated() {
        UUID userId = UUID.randomUUID();
        PushSubscription removed = subscription(userId);
        removed.setRevoked(true);
        when(pushSubscriptionRepository.findByEndpoint(removed.getEndpoint()))
                .thenReturn(Optional.of(removed));

        assertThatThrownBy(() -> deviceService.registerSubscription(
                userId,
                removed.getEndpoint(),
                "new-public-key",
                "new-auth-key",
                "browser",
                false))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("removed");

        verify(pushSubscriptionRepository, never()).save(any());
    }

    @Test
    void explicitPermissionFlowCanReactivateRemovedDevice() {
        UUID userId = UUID.randomUUID();
        PushSubscription removed = subscription(userId);
        removed.setRevoked(true);
        removed.setRevocationReason("USER");
        when(pushSubscriptionRepository.findByEndpoint(removed.getEndpoint()))
                .thenReturn(Optional.of(removed));
        when(pushSubscriptionRepository.save(removed)).thenReturn(removed);

        PushSubscription result = deviceService.registerSubscription(
                userId,
                removed.getEndpoint(),
                "new-public-key",
                "new-auth-key",
                "browser",
                true);

        assertThat(result.isRevoked()).isFalse();
        assertThat(result.getRevocationReason()).isNull();
    }

    @Test
    void removingDevicePersistsRevocationInsteadOfDeletingRecord() {
        UUID userId = UUID.randomUUID();
        PushSubscription subscription = subscription(userId);
        when(pushSubscriptionRepository.findById(subscription.getId()))
                .thenReturn(Optional.of(subscription));

        deviceService.removeSubscription(subscription.getId(), userId);

        assertThat(subscription.isRevoked()).isTrue();
        assertThat(subscription.getRevocationReason()).isEqualTo("USER");
        verify(pushSubscriptionRepository).save(subscription);
        verify(pushSubscriptionRepository, never()).delete(any());
    }

    private PushSubscription subscription(UUID userId) {
        PushSubscription subscription = new PushSubscription();
        subscription.setId(UUID.randomUUID());
        subscription.setUserId(userId);
        subscription.setEndpoint("https://updates.push.services.mozilla.com/wpush/v2/example");
        return subscription;
    }
}
