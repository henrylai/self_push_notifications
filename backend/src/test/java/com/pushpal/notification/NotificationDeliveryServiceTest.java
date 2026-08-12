package com.pushpal.notification;

import com.pushpal.auth.DeliveryTokenProvider;
import com.pushpal.device.PushSubscription;
import com.pushpal.device.PushSubscriptionRepository;
import com.pushpal.push.NotificationProvider;
import com.pushpal.push.PushService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationDeliveryServiceTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private PushSubscriptionRepository pushSubscriptionRepository;

    @Mock
    private PushService pushService;

    @Mock
    private DeliveryTokenProvider deliveryTokenProvider;

    private NotificationDeliveryService deliveryService;

    @BeforeEach
    void setUp() {
        deliveryService = new NotificationDeliveryService(
                notificationService,
                pushSubscriptionRepository,
                pushService,
                deliveryTokenProvider);
        ReflectionTestUtils.setField(deliveryService, "apiBaseUrl", "https://api.pushpal.test");
    }

    @Test
    void pushContainsScopedDeliveryTokenAndSupportsNullBody() {
        Notification notification = notification();
        PushSubscription subscription = new PushSubscription();
        subscription.setId(UUID.randomUUID());
        when(notificationService.lockDueNotification(eq(notification.getId()), any()))
                .thenReturn(Optional.of(notification));
        when(pushSubscriptionRepository.findByUserId(notification.getRecipientId()))
                .thenReturn(List.of(subscription));
        when(deliveryTokenProvider.generateToken(
                notification.getId(), notification.getRecipientId()))
                .thenReturn("delivery-token");
        when(pushService.sendToAll(eq(List.of(subscription)), any()))
                .thenReturn(new PushService.AggregatedResult(1, 0, null));
        ArgumentCaptor<NotificationProvider.NotificationPayload> payloadCaptor =
                ArgumentCaptor.forClass(NotificationProvider.NotificationPayload.class);

        deliveryService.process(notification.getId());

        verify(pushService).sendToAll(eq(List.of(subscription)), payloadCaptor.capture());
        NotificationProvider.NotificationPayload payload = payloadCaptor.getValue();
        assertThat(payload.body()).isNull();
        assertThat(payload.data())
                .containsEntry("deliveryToken", "delivery-token")
                .containsEntry("icon", "bell")
                .doesNotContainKey("token");
        verify(notificationService).markAsSent(notification.getId());
    }

    @Test
    void failedFirstAttemptIsScheduledForRetry() {
        Notification notification = notification();
        when(notificationService.lockDueNotification(eq(notification.getId()), any()))
                .thenReturn(Optional.of(notification));
        when(pushSubscriptionRepository.findByUserId(notification.getRecipientId()))
                .thenReturn(List.of());

        deliveryService.process(notification.getId());

        verify(notificationService).markForRetry(
                notification.getId(), "No push subscriptions registered");
    }

    @Test
    void storesThePushProviderFailureReasonForRetry() {
        Notification notification = notification();
        PushSubscription subscription = new PushSubscription();
        subscription.setId(UUID.randomUUID());
        when(notificationService.lockDueNotification(eq(notification.getId()), any()))
                .thenReturn(Optional.of(notification));
        when(pushSubscriptionRepository.findByUserId(notification.getRecipientId()))
                .thenReturn(List.of(subscription));
        when(pushService.sendToAll(eq(List.of(subscription)), any()))
                .thenReturn(new PushService.AggregatedResult(0, 1, "Web Push not configured"));

        deliveryService.process(notification.getId());

        verify(notificationService).markForRetry(notification.getId(), "Web Push not configured");
    }

    private Notification notification() {
        Notification notification = new Notification();
        notification.setId(UUID.randomUUID());
        notification.setSenderId(UUID.randomUUID());
        notification.setRecipientId(UUID.randomUUID());
        notification.setTitle("Reminder");
        notification.setBody(null);
        notification.setScheduledTime(Instant.now().minusSeconds(1));
        notification.setStatus(NotificationStatus.PENDING);
        return notification;
    }
}
