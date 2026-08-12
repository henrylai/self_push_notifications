package com.pushpal.push;

import com.pushpal.device.PushSubscription;
import com.pushpal.device.PushSubscriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PushServiceTest {

    @Mock
    private NotificationProvider notificationProvider;

    @Mock
    private PushSubscriptionRepository pushSubscriptionRepository;

    private PushService pushService;

    @BeforeEach
    void setUp() {
        pushService = new PushService(notificationProvider, pushSubscriptionRepository);
    }

    @Test
    void aggregatesResultsAndRemovesOnlyStaleSubscriptions() {
        PushSubscription delivered = subscription();
        PushSubscription stale = subscription();
        PushSubscription failed = subscription();
        NotificationProvider.NotificationPayload payload =
                new NotificationProvider.NotificationPayload("Title", "Body", Map.of());
        when(notificationProvider.send(delivered, payload)).thenReturn(NotificationProvider.SendResult.ok());
        when(notificationProvider.send(stale, payload)).thenReturn(
                NotificationProvider.SendResult.subscriptionGone("Subscription no longer valid (HTTP 410)"));
        when(notificationProvider.send(failed, payload)).thenReturn(
                NotificationProvider.SendResult.failure("Push service returned HTTP 500"));

        PushService.AggregatedResult result = pushService.sendToAll(
                List.of(delivered, stale, failed), payload);

        assertThat(result.successCount()).isEqualTo(1);
        assertThat(result.failureCount()).isEqualTo(2);
        assertThat(result.firstError()).isEqualTo("Subscription no longer valid (HTTP 410)");
        verify(pushSubscriptionRepository).delete(stale);
        verifyNoMoreInteractions(pushSubscriptionRepository);
    }

    private PushSubscription subscription() {
        PushSubscription subscription = new PushSubscription();
        subscription.setId(UUID.randomUUID());
        return subscription;
    }
}
