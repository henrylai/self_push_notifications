package com.pushpal.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping
    public ResponseEntity<NotificationDto> createNotification(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody CreateNotificationRequest request) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        Notification notification = notificationService.createNotification(userId, request);
        return ResponseEntity.ok(NotificationDto.fromEntity(notification));
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> listNotifications(
            @AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        var received = notificationService.getReceivedNotifications(userId)
                .stream().map(NotificationDto::fromEntity).toList();
        var sent = notificationService.getSentNotifications(userId)
                .stream().map(NotificationDto::fromEntity).toList();
        return ResponseEntity.ok(Map.of("received", received, "sent", sent));
    }

    @GetMapping("/{id}")
    public ResponseEntity<NotificationDto> getNotification(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID id) {
        Notification notification = notificationService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found"));
        return ResponseEntity.ok(NotificationDto.fromEntity(notification));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> cancelNotification(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID id) {
        notificationService.cancelNotification(id);
        return ResponseEntity.ok(Map.of("message", "Notification cancelled"));
    }

    @PostMapping("/{id}/viewed")
    public ResponseEntity<NotificationDto> markAsViewed(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID id) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        Notification notification = notificationService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found"));
        if (!notification.getRecipientId().equals(userId)) {
            return ResponseEntity.status(403).build();
        }
        Notification updated = notificationService.markAsViewed(id);
        return ResponseEntity.ok(NotificationDto.fromEntity(updated));
    }

    @PostMapping("/{id}/delivered")
    public ResponseEntity<NotificationDto> markAsDelivered(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID id) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        Notification notification = notificationService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found"));
        if (!notification.getRecipientId().equals(userId)) {
            return ResponseEntity.status(403).build();
        }
        Notification updated = notificationService.markAsDelivered(id);
        return ResponseEntity.ok(NotificationDto.fromEntity(updated));
    }
}
