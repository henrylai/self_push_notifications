package com.pushpal.notification;

import com.pushpal.auth.DeliveryTokenProvider;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Validated
public class NotificationController {

    private final NotificationService notificationService;
    private final DeliveryTokenProvider deliveryTokenProvider;
    private final NotificationDtoMapper notificationDtoMapper;

    @PostMapping
    public ResponseEntity<NotificationDto> createNotification(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CreateNotificationRequest request) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        Notification notification = notificationService.createNotification(userId, request);
        return ResponseEntity.ok(notificationDtoMapper.map(notification));
    }

    @GetMapping
    public ResponseEntity<NotificationListDto> listNotifications(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "50") @Min(1) @Max(100) int size) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        PageRequest pageable = PageRequest.of(page, size);
        var received = notificationService.getReceivedNotifications(userId, pageable);
        var sent = notificationService.getSentNotifications(userId, pageable);
        return ResponseEntity.ok(new NotificationListDto(
                notificationDtoMapper.mapAll(received.getContent()),
                notificationDtoMapper.mapAll(sent.getContent()),
                page,
                size,
                received.hasNext(),
                sent.hasNext()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<NotificationDto> getNotification(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID id) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        Notification notification = notificationService.findAccessibleById(id, userId);
        return ResponseEntity.ok(notificationDtoMapper.map(notification));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> cancelNotification(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID id) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        notificationService.cancelNotification(id, userId);
        return ResponseEntity.ok(Map.of("message", "Notification cancelled"));
    }

    @PostMapping("/{id}/viewed")
    public ResponseEntity<NotificationDto> markAsViewed(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID id) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        Notification updated = notificationService.markAsViewed(id, userId);
        return ResponseEntity.ok(notificationDtoMapper.map(updated));
    }

    @PostMapping("/{id}/delivered")
    public ResponseEntity<NotificationDto> markAsDelivered(
            @PathVariable UUID id,
            @RequestHeader(name = "X-PushPal-Delivery-Token", required = false) String deliveryToken) {
        if (!deliveryTokenProvider.validates(deliveryToken, id)) {
            throw new AccessDeniedException("Invalid delivery token");
        }
        Notification updated = notificationService.markAsDelivered(id);
        return ResponseEntity.ok(notificationDtoMapper.map(updated));
    }
}
