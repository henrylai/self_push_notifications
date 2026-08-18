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
import static org.mockito.Mockito.never;
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

    @Mock
    private NotificationDeliveryRepository notificationDeliveryRepository;

    private NotificationDeliveryService deliveryService;

    @BeforeEach
    void setUp() {
        deliveryService = new NotificationDeliveryService(
                notificationService,
                pushSubscriptionRepository,
                pushService,
                deliveryTokenProvider,
                notificationDeliveryRepository);
        ReflectionTestUtils.setField(deliveryService, "apiBaseUrl", "https://api.pushpal.test");
    }

    @Test
    void pushContainsScopedDeliveryTokenAndSupportsNullBody() {
        Notification notification = notification();
        PushSubscription subscription = new PushSubscription();
        subscription.setId(UUID.randomUUID());
        when(notificationDeliveryRepository.save(any(NotificationDelivery.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(notificationService.lockDueNotification(eq(notification.getId()), any()))
                .thenReturn(Optional.of(notification));
        when(pushSubscriptionRepository.findByUserIdAndRevokedFalse(notification.getRecipientId()))
                .thenReturn(List.of(subscription));
        when(deliveryTokenProvider.generateToken(
                notification.getId(), notification.getRecipientId()))
                .thenReturn("delivery-token");
        when(pushService.sendToAll(eq(List.of(subscription)), any()))
                .thenReturn(result(subscription, true, false, null));
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
        when(pushSubscriptionRepository.findByUserIdAndRevokedFalse(notification.getRecipientId()))
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
        when(notificationDeliveryRepository.save(any(NotificationDelivery.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(notificationService.lockDueNotification(eq(notification.getId()), any()))
                .thenReturn(Optional.of(notification));
        when(pushSubscriptionRepository.findByUserIdAndRevokedFalse(notification.getRecipientId()))
                .thenReturn(List.of(subscription));
        when(pushService.sendToAll(eq(List.of(subscription)), any()))
                .thenReturn(result(subscription, false, false, "Web Push not configured"));

        deliveryService.process(notification.getId());

        verify(notificationService).markForRetry(notification.getId(), "Web Push not configured");
    }

    @Test
    void retryTargetsOnlyDevicesThatFailedTheFirstAttempt() {
        Notification notification = notification();
        notification.setRetryCount(1);
        PushSubscription alreadySent = subscription();
        PushSubscription retryTarget = subscription();
        NotificationDelivery sentDelivery = delivery(
                notification, alreadySent, NotificationDeliveryStatus.SENT);
        NotificationDelivery pendingDelivery = delivery(
                notification, retryTarget, NotificationDeliveryStatus.PENDING);
        pendingDelivery.setNextAttemptAt(Instant.now().minusSeconds(1));
        when(notificationService.lockDueNotification(eq(notification.getId()), any()))
                .thenReturn(Optional.of(notification));
        when(pushSubscriptionRepository.findByUserIdAndRevokedFalse(notification.getRecipientId()))
                .thenReturn(List.of(alreadySent, retryTarget));
        when(notificationDeliveryRepository.findByNotificationId(notification.getId()))
                .thenReturn(List.of(sentDelivery, pendingDelivery));
        when(pushService.sendToAll(eq(List.of(retryTarget)), any()))
                .thenReturn(result(retryTarget, true, false, null));

        deliveryService.process(notification.getId());

        verify(pushService).sendToAll(eq(List.of(retryTarget)), any());
        verify(notificationService).markAsSent(notification.getId());
        verify(notificationService, never()).markAsFailed(any(), any());
        assertThat(sentDelivery.getAttemptCount()).isZero();
        assertThat(pendingDelivery.getStatus()).isEqualTo(NotificationDeliveryStatus.SENT);
        assertThat(pendingDelivery.getAttemptCount()).isEqualTo(1);
    }

    @Test
    void partialFirstAttemptSchedulesRetryWithoutResendingSuccessfulDevice() {
        Notification notification = notification();
        PushSubscription delivered = subscription();
        PushSubscription failed = subscription();
        when(notificationService.lockDueNotification(eq(notification.getId()), any()))
                .thenReturn(Optional.of(notification));
        when(pushSubscriptionRepository.findByUserIdAndRevokedFalse(notification.getRecipientId()))
                .thenReturn(List.of(delivered, failed));
        when(notificationDeliveryRepository.save(any(NotificationDelivery.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(pushService.sendToAll(eq(List.of(delivered, failed)), any()))
                .thenReturn(new PushService.AggregatedResult(List.of(
                        new PushService.SubscriptionResult(delivered.getId(), true, false, null),
                        new PushService.SubscriptionResult(
                                failed.getId(), false, false, "Push HTTP 500"))));

        deliveryService.process(notification.getId());

        verify(notificationService).markForRetry(notification.getId(), "Push HTTP 500");
    }

    private PushService.AggregatedResult result(PushSubscription subscription,
                                                boolean success,
                                                boolean gone,
                                                String error) {
        return new PushService.AggregatedResult(List.of(new PushService.SubscriptionResult(
                subscription.getId(), success, gone, error)));
    }

    private PushSubscription subscription() {
        PushSubscription subscription = new PushSubscription();
        subscription.setId(UUID.randomUUID());
        return subscription;
    }

    private NotificationDelivery delivery(Notification notification,
                                          PushSubscription subscription,
                                          NotificationDeliveryStatus status) {
        NotificationDelivery delivery = new NotificationDelivery();
        delivery.setId(UUID.randomUUID());
        delivery.setNotificationId(notification.getId());
        delivery.setSubscriptionId(subscription.getId());
        delivery.setStatus(status);
        return delivery;
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
