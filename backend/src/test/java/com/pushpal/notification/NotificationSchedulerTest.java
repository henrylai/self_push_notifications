package com.pushpal.notification;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationSchedulerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private NotificationDeliveryService notificationDeliveryService;

    private NotificationScheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = new NotificationScheduler(notificationService, notificationDeliveryService);
    }

    @Test
    void continuesProcessingWhenOneDeliveryFails() {
        Notification first = notification();
        Notification second = notification();
        when(notificationService.getPendingNotifications(any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(first, second)));
        doThrow(new IllegalStateException("temporary provider failure"))
                .when(notificationDeliveryService).process(first.getId());

        scheduler.processPendingNotifications();

        verify(notificationDeliveryService).process(first.getId());
        verify(notificationDeliveryService).process(second.getId());
    }

    private Notification notification() {
        Notification notification = new Notification();
        notification.setId(UUID.randomUUID());
        return notification;
    }
}
