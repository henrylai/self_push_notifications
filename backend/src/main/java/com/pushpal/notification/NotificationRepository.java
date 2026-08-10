package com.pushpal.notification;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    List<Notification> findByRecipientIdOrderByScheduledTimeDesc(UUID recipientId);

    List<Notification> findBySenderIdOrderByCreatedAtDesc(UUID senderId);

    @Query("SELECT n FROM Notification n WHERE n.status = 'PENDING' " +
            "AND (n.scheduledTime <= :now OR (n.nextAttemptAt IS NOT NULL AND n.nextAttemptAt <= :now))")
    Page<Notification> findPendingNotifications(@Param("now") Instant now, Pageable pageable);
}
