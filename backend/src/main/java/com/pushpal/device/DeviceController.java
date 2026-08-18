package com.pushpal.device;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/devices")
@RequiredArgsConstructor
public class DeviceController {

    private final DeviceService deviceService;

    @PostMapping("/register")
    public ResponseEntity<RegisterDeviceResponse> registerSubscription(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody RegisterDeviceRequest request) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        PushSubscription subscription = deviceService.registerSubscription(
                userId,
                request.endpoint(),
                request.p256dh(),
                request.authKey(),
                request.userAgent(),
                request.reactivate());
        return ResponseEntity.ok(new RegisterDeviceResponse(
                "Subscription registered successfully", subscription.getId()));
    }

    @PostMapping("/unregister")
    public ResponseEntity<Map<String, String>> unregisterCurrentSubscription(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UnregisterDeviceRequest request) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        deviceService.removeSubscriptionByEndpoint(request.endpoint(), userId);
        return ResponseEntity.ok(Map.of("message", "Subscription removed"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> removeSubscription(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID id) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        deviceService.removeSubscription(id, userId);
        return ResponseEntity.ok(Map.of("message", "Subscription removed"));
    }

    @GetMapping
    public ResponseEntity<List<DeviceDto>> listDevices(
            @AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        List<DeviceDto> devices = deviceService.getUserSubscriptions(userId).stream()
                .map(DeviceDto::fromEntity)
                .toList();
        return ResponseEntity.ok(devices);
    }
}
