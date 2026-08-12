package com.pushpal.notification;

import com.pushpal.auth.DeliveryTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationControllerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private DeliveryTokenProvider deliveryTokenProvider;

    private NotificationController controller;

    @BeforeEach
    void setUp() {
        controller = new NotificationController(notificationService, deliveryTokenProvider);
    }

    @Test
    void listsSelfRemindersOnlyInSentNotifications() {
        UUID userId = UUID.randomUUID();
        Notification selfReminder = notification(userId, userId);
        Notification receivedReminder = notification(UUID.randomUUID(), userId);
        when(notificationService.getReceivedNotifications(userId))
                .thenReturn(List.of(selfReminder, receivedReminder));
        when(notificationService.getSentNotifications(userId))
                .thenReturn(List.of(selfReminder));

        ResponseEntity<Map<String, Object>> response = controller.listNotifications(userDetails(userId));

        assertThat(notifications(response.getBody(), "received"))
                .extracting(NotificationDto::id)
                .containsExactly(receivedReminder.getId());
        assertThat(notifications(response.getBody(), "sent"))
                .extracting(NotificationDto::id)
                .containsExactly(selfReminder.getId());
    }

    @SuppressWarnings("unchecked")
    private List<NotificationDto> notifications(Map<String, Object> body, String key) {
        return (List<NotificationDto>) body.get(key);
    }

    private UserDetails userDetails(UUID userId) {
        return org.springframework.security.core.userdetails.User.withUsername(userId.toString())
                .password("unused")
                .authorities("USER")
                .build();
    }

    private Notification notification(UUID senderId, UUID recipientId) {
        Notification notification = new Notification();
        notification.setId(UUID.randomUUID());
        notification.setSenderId(senderId);
        notification.setRecipientId(recipientId);
        notification.setTitle("Reminder");
        notification.setScheduledTime(Instant.now().plusSeconds(60));
        notification.setStatus(NotificationStatus.PENDING);
        return notification;
    }
}
