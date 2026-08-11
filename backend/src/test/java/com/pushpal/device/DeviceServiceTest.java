package com.pushpal.device;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
                "browser"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("HTTPS");
    }
}
