package com.pushpal.auth;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

@Component
public class DeliveryTokenProvider {

    private static final String KEY_CONTEXT = ":pushpal-delivery-token";

    private final SecretKey key;
    private final long expirationHours;

    public DeliveryTokenProvider(
            @Value("${app.jwt.secret}") String jwtSecret,
            @Value("${app.delivery-token.expiration-hours:24}") long expirationHours) {
        this.key = Keys.hmacShaKeyFor(deriveKey(jwtSecret));
        this.expirationHours = expirationHours;
    }

    public String generateToken(UUID notificationId, UUID recipientId) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(notificationId.toString())
                .claim("recipientId", recipientId.toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(expirationHours, ChronoUnit.HOURS)))
                .signWith(key)
                .compact();
    }

    public boolean validates(String token, UUID notificationId) {
        if (token == null || token.isBlank()) {
            return false;
        }
        try {
            var claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return notificationId.toString().equals(claims.getSubject())
                    && claims.get("recipientId", String.class) != null;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private static byte[] deriveKey(String jwtSecret) {
        if (jwtSecret == null || jwtSecret.isBlank()) {
            throw new IllegalStateException("JWT_SECRET must be configured");
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return digest.digest((jwtSecret + KEY_CONTEXT).getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }
}
