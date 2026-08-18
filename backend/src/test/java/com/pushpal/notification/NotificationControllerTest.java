package com.pushpal.notification;

import com.pushpal.auth.DeliveryTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationControllerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private DeliveryTokenProvider deliveryTokenProvider;

    @Mock
    private NotificationDtoMapper notificationDtoMapper;

    private NotificationController controller;

    @BeforeEach
    void setUp() {
        controller = new NotificationController(
                notificationService, deliveryTokenProvider, notificationDtoMapper);
    }

    @Test
    void listsSelfRemindersOnlyInSentNotifications() {
        UUID userId = UUID.randomUUID();
        Notification selfReminder = notification(userId, userId);
        Notification receivedReminder = notification(UUID.randomUUID(), userId);
        NotificationDto receivedDto = NotificationDto.fromEntity(receivedReminder);
        NotificationDto sentDto = NotificationDto.fromEntity(selfReminder);
        when(notificationService.getReceivedNotifications(
                org.mockito.ArgumentMatchers.eq(userId), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(receivedReminder)));
        when(notificationService.getSentNotifications(
                org.mockito.ArgumentMatchers.eq(userId), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(selfReminder)));
        when(notificationDtoMapper.mapAll(List.of(receivedReminder))).thenReturn(List.of(receivedDto));
        when(notificationDtoMapper.mapAll(List.of(selfReminder))).thenReturn(List.of(sentDto));

        ResponseEntity<NotificationListDto> response = controller.listNotifications(
                userDetails(userId), 0, 50);

        assertThat(response.getBody().received())
                .extracting(NotificationDto::id)
                .containsExactly(receivedReminder.getId());
        assertThat(response.getBody().sent())
                .extracting(NotificationDto::id)
                .containsExactly(selfReminder.getId());
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
