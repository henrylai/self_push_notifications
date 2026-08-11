package com.pushpal.auth;

import com.pushpal.user.User;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenProviderTest {

    private static final String SECRET = "test-secret-that-is-long-enough-for-hs256-signing";

    @Test
    void malformedAndInvalidSignatureTokensAreRejected() {
        JwtTokenProvider provider = new JwtTokenProvider(SECRET, 7);
        JwtTokenProvider otherProvider = new JwtTokenProvider(
                "different-secret-that-is-long-enough-for-hs256", 7);
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("user@example.com");
        user.setName("User");

        String validToken = provider.generateToken(user);
        String invalidSignature = otherProvider.generateToken(user);

        assertThat(provider.validateToken(validToken)).isTrue();
        assertThat(provider.validateToken("not-a-token")).isFalse();
        assertThat(provider.validateToken(invalidSignature)).isFalse();
    }
}
