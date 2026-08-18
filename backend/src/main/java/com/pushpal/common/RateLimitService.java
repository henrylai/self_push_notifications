package com.pushpal.common;

import com.pushpal.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RateLimitService {

    private static final Duration EVENT_RETENTION = Duration.ofDays(30);

    private final RateLimitEventRepository rateLimitEventRepository;
    private final UserRepository userRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void checkAndRecord(UUID userId,
                               String action,
                               int maximumAttempts,
                               Duration window,
                               String errorMessage) {
        userRepository.findByIdForUpdate(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        Instant now = Instant.now();
        long attempts = rateLimitEventRepository.countByUserIdAndActionAndCreatedAtAfter(
                userId, action, now.minus(window));
        if (attempts >= maximumAttempts) {
            throw new RateLimitExceededException(errorMessage);
        }

        RateLimitEvent event = new RateLimitEvent();
        event.setUserId(userId);
        event.setAction(action);
        rateLimitEventRepository.save(event);
        rateLimitEventRepository.deleteCreatedBefore(now.minus(EVENT_RETENTION));
    }
}
