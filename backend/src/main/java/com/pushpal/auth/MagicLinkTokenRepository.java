package com.pushpal.auth;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface MagicLinkTokenRepository extends JpaRepository<MagicLinkToken, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT token FROM MagicLinkToken token WHERE token.tokenHash = :tokenHash")
    Optional<MagicLinkToken> findByTokenHashForUpdate(@Param("tokenHash") String tokenHash);

    long countByEmailAndCreatedAtAfter(String email, Instant createdAfter);
}
