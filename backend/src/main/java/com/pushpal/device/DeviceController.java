package com.pushpal.device;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/devices")
@RequiredArgsConstructor
public class DeviceController {

    private final DeviceService deviceService;

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> registerSubscription(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, String> request) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        deviceService.registerSubscription(
                userId,
                request.get("endpoint"),
                request.get("p256dh"),
                request.get("auth"),
                request.get("userAgent"));
        return ResponseEntity.ok(Map.of("message", "Subscription registered successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> removeSubscription(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID id) {
        deviceService.removeSubscription(id);
        return ResponseEntity.ok(Map.of("message", "Subscription removed"));
    }

    @GetMapping
    public ResponseEntity<List<PushSubscription>> listDevices(
            @AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        return ResponseEntity.ok(deviceService.getUserSubscriptions(userId));
    }
}
