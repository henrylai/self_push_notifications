package com.pushpal.notification;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    @Query("SELECT n FROM Notification n WHERE n.recipientId = :recipientId "
            + "AND (n.senderId IS NULL OR n.senderId <> :recipientId) "
            + "ORDER BY n.scheduledTime DESC")
    Page<Notification> findReceivedNotifications(@Param("recipientId") UUID recipientId,
                                                 Pageable pageable);

    Page<Notification> findBySenderIdOrderByCreatedAtDesc(UUID senderId, Pageable pageable);

    long countBySenderIdAndCreatedAtAfter(UUID senderId, Instant createdAfter);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT n FROM Notification n WHERE n.id = :id")
    Optional<Notification> findByIdForUpdate(@Param("id") UUID id);

    @Query("SELECT n FROM Notification n WHERE n.status = 'PENDING' "
            + "AND ((n.retryCount = 0 AND n.scheduledTime <= :now) OR "
            + "(n.retryCount > 0 AND n.nextAttemptAt IS NOT NULL AND n.nextAttemptAt <= :now)) "
            + "ORDER BY COALESCE(n.nextAttemptAt, n.scheduledTime)")
    Page<Notification> findPendingNotifications(@Param("now") Instant now, Pageable pageable);

    @Modifying
    @Query("UPDATE Notification n SET n.status = 'CANCELLED', n.nextAttemptAt = NULL, "
            + "n.failureReason = NULL WHERE n.status = 'PENDING' AND "
            + "((n.senderId = :firstUserId AND n.recipientId = :secondUserId) OR "
            + "(n.senderId = :secondUserId AND n.recipientId = :firstUserId))")
    int cancelPendingNotificationsBetweenUsers(@Param("firstUserId") UUID firstUserId,
                                               @Param("secondUserId") UUID secondUserId);
}
