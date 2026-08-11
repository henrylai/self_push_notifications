package com.pushpal.device;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeviceService {

    private final PushSubscriptionRepository pushSubscriptionRepository;

    @Transactional
    public PushSubscription registerSubscription(UUID userId, String endpoint,
                                                  String p256dh, String authKey,
                                                  String userAgent) {
        validateEndpoint(endpoint);
        var existing = pushSubscriptionRepository.findByEndpoint(endpoint);

        if (existing.isPresent()) {
            PushSubscription subscription = existing.get();
            if (!subscription.getUserId().equals(userId)
                    && (!subscription.getP256dh().equals(p256dh)
                    || !subscription.getAuthKey().equals(authKey))) {
                throw new IllegalStateException("Push subscription is already registered");
            }
            subscription.setUserId(userId);
            subscription.setP256dh(p256dh);
            subscription.setAuthKey(authKey);
            subscription.setUserAgent(userAgent);
            subscription.setLastUsedAt(Instant.now());
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
        return pushSubscriptionRepository.findByUserId(userId);
    }

    @Transactional
    public void removeSubscription(UUID id, UUID userId) {
        PushSubscription subscription = pushSubscriptionRepository.findById(id)
                .filter(item -> item.getUserId().equals(userId))
                .orElseThrow(() -> new EntityNotFoundException("Device not found"));
        pushSubscriptionRepository.delete(subscription);
    }

    private void validateEndpoint(String endpoint) {
        try {
            URI uri = URI.create(endpoint);
            if (!"https".equalsIgnoreCase(uri.getScheme()) || uri.getHost() == null) {
                throw new IllegalArgumentException("Push endpoint must be a valid HTTPS URL");
            }
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Push endpoint must be a valid HTTPS URL");
        }
    }
}
