package com.pushpal.auth;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class MagicLinkTokenRepositoryTest {

    @Autowired
    private MagicLinkTokenRepository repository;

    @Test
    void consumesValidTokenExactlyOnce() {
        MagicLinkToken token = token("a".repeat(64), Instant.now().plusSeconds(300));
        repository.saveAndFlush(token);

        int firstAttempt = repository.consumeValidToken(token.getTokenHash(), Instant.now());
        int secondAttempt = repository.consumeValidToken(token.getTokenHash(), Instant.now());

        assertThat(firstAttempt).isEqualTo(1);
        assertThat(secondAttempt).isZero();
        assertThat(repository.findByTokenHash(token.getTokenHash()).orElseThrow().getUsedAt())
                .isNotNull();
    }

    @Test
    void doesNotConsumeExpiredOrUnknownToken() {
        MagicLinkToken expired = token("b".repeat(64), Instant.now().minusSeconds(1));
        repository.saveAndFlush(expired);

        assertThat(repository.consumeValidToken(expired.getTokenHash(), Instant.now())).isZero();
        assertThat(repository.consumeValidToken("c".repeat(64), Instant.now())).isZero();
    }

    private MagicLinkToken token(String tokenHash, Instant expiresAt) {
        MagicLinkToken token = new MagicLinkToken();
        token.setEmail("user@example.com");
        token.setTokenHash(tokenHash);
        token.setExpiresAt(expiresAt);
        return token;
    }
}
