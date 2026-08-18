package com.pushpal.common;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.UUID;

public interface RateLimitEventRepository extends JpaRepository<RateLimitEvent, UUID> {

    long countByUserIdAndActionAndCreatedAtAfter(UUID userId, String action, Instant createdAfter);

    @Modifying
    @Query("DELETE FROM RateLimitEvent event WHERE event.createdAt < :cutoff")
    int deleteCreatedBefore(@Param("cutoff") Instant cutoff);
}
