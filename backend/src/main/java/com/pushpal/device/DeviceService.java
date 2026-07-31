package com.pushpal.device;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        // Check if endpoint already exists, update if so
        var existing = pushSubscriptionRepository.findByUserId(userId).stream()
                .filter(s -> s.getEndpoint().equals(endpoint))
                .findFirst();

        if (existing.isPresent()) {
            PushSubscription subscription = existing.get();
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
    public void removeSubscription(UUID id) {
        pushSubscriptionRepository.deleteById(id);
    }
}
