package com.pushpal.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface MagicLinkTokenRepository extends JpaRepository<MagicLinkToken, UUID> {

    Optional<MagicLinkToken> findByTokenHash(String tokenHash);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE MagicLinkToken token SET token.usedAt = :usedAt "
            + "WHERE token.tokenHash = :tokenHash AND token.usedAt IS NULL "
            + "AND token.expiresAt > :usedAt")
    int consumeValidToken(@Param("tokenHash") String tokenHash,
                          @Param("usedAt") Instant usedAt);

    long countByEmailAndCreatedAtAfter(String email, Instant createdAfter);
}
