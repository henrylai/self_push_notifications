package com.pushpal.device;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeviceService {

    private static final Set<String> ALLOWED_PUSH_HOST_SUFFIXES = Set.of(
            "fcm.googleapis.com",
            "push.services.mozilla.com",
            "updates.push.services.mozilla.com",
            "push.apple.com",
            "notify.windows.com");

    private final PushSubscriptionRepository pushSubscriptionRepository;

    @Transactional
    public PushSubscription registerSubscription(UUID userId, String endpoint,
                                                  String p256dh, String authKey,
                                                  String userAgent, boolean reactivate) {
        validateEndpoint(endpoint);
        var existing = pushSubscriptionRepository.findByEndpoint(endpoint);

        if (existing.isPresent()) {
            PushSubscription subscription = existing.get();
            if (!subscription.getUserId().equals(userId)) {
                throw new IllegalStateException("Push subscription is already registered");
            }
            if (subscription.isRevoked() && !reactivate) {
                throw new IllegalStateException(
                        "This device was removed. Enable notifications again to reactivate it.");
            }
            subscription.setUserId(userId);
            subscription.setP256dh(p256dh);
            subscription.setAuthKey(authKey);
            subscription.setUserAgent(userAgent);
            subscription.setLastUsedAt(Instant.now());
            subscription.setRevoked(false);
            subscription.setRevocationReason(null);
            return pushSubscriptionRepository.save(subscription);
        }

        PushSubscription subscription = new PushSubscription();
        subscription.setUserId(userId);
        subscription.setEndpoint(endpoint);
        subscription.setP256dh(p256dh);
        subscription.setAuthKey(authKey);
        subscription.setUserAgent(userAgent);
        return pushSubscriptionRepository.save(subscription);
    }

    public List<PushSubscription> getUserSubscriptions(UUID userId) {
        return pushSubscriptionRepository.findByUserIdAndRevokedFalse(userId);
    }

    @Transactional
    public void removeSubscription(UUID id, UUID userId) {
        PushSubscription subscription = pushSubscriptionRepository.findById(id)
                .filter(item -> item.getUserId().equals(userId))
                .orElseThrow(() -> new EntityNotFoundException("Device not found"));
        revoke(subscription, "USER");
    }

    @Transactional
    public void removeSubscriptionByEndpoint(String endpoint, UUID userId) {
        PushSubscription subscription = pushSubscriptionRepository.findByEndpoint(endpoint)
                .filter(item -> item.getUserId().equals(userId))
                .orElseThrow(() -> new EntityNotFoundException("Device not found"));
        revoke(subscription, "USER");
    }

    private void validateEndpoint(String endpoint) {
        try {
            URI uri = URI.create(endpoint);
            String host = uri.getHost();
            if (!"https".equalsIgnoreCase(uri.getScheme())
                    || host == null
                    || (uri.getPort() != -1 && uri.getPort() != 443)
                    || uri.getUserInfo() != null
                    || !isAllowedPushHost(host)) {
                throw new IllegalArgumentException("Push endpoint must be a valid HTTPS URL");
            }
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Push endpoint must be a valid HTTPS URL");
        }
    }

    private boolean isAllowedPushHost(String host) {
        String normalized = host.toLowerCase(Locale.ROOT);
        return ALLOWED_PUSH_HOST_SUFFIXES.stream()
                .anyMatch(suffix -> normalized.equals(suffix)
                        || normalized.endsWith("." + suffix));
    }

    private void revoke(PushSubscription subscription, String reason) {
        subscription.setRevoked(true);
        subscription.setRevocationReason(reason);
        subscription.setLastUsedAt(Instant.now());
        pushSubscriptionRepository.save(subscription);
    }
}
