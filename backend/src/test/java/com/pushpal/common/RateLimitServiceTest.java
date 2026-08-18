package com.pushpal.common;

import com.pushpal.user.User;
import com.pushpal.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RateLimitServiceTest {

    @Mock
    private RateLimitEventRepository eventRepository;

    @Mock
    private UserRepository userRepository;

    private RateLimitService service;

    @BeforeEach
    void setUp() {
        service = new RateLimitService(eventRepository, userRepository);
    }

    @Test
    void recordsAnAttemptBelowLimit() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findByIdForUpdate(userId)).thenReturn(Optional.of(new User()));
        when(eventRepository.countByUserIdAndActionAndCreatedAtAfter(
                eq(userId), eq("INVITE_ACCEPT"), any())).thenReturn(4L);

        service.checkAndRecord(
                userId, "INVITE_ACCEPT", 5, Duration.ofHours(1), "Too many attempts");

        verify(eventRepository).save(any(RateLimitEvent.class));
    }

    @Test
    void rejectsAndDoesNotRecordAttemptAtLimit() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findByIdForUpdate(userId)).thenReturn(Optional.of(new User()));
        when(eventRepository.countByUserIdAndActionAndCreatedAtAfter(
                eq(userId), eq("INVITE_ACCEPT"), any())).thenReturn(5L);

        assertThatThrownBy(() -> service.checkAndRecord(
                userId, "INVITE_ACCEPT", 5, Duration.ofHours(1), "Too many attempts"))
                .isInstanceOf(RateLimitExceededException.class)
                .hasMessage("Too many attempts");

        verify(eventRepository, never()).save(any());
    }
}
