package com.pushpal.auth;

import com.pushpal.user.User;
import com.pushpal.user.UserRepository;
import com.pushpal.common.RateLimitExceededException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int TOKEN_BYTES = 32; // 64 hex characters
    private static final int MAX_MAGIC_LINKS_PER_HOUR = 5;
    private static final String EMAIL_REGEX = "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$";

    private final UserRepository userRepository;
    private final MagicLinkTokenRepository magicLinkTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final EmailService emailService;
    private final GoogleOAuthService googleOAuthService;

    @Value("${app.magic-link.expiration-minutes:15}")
    private long magicLinkExpirationMinutes;

    @Value("${app.magic-link.base-url:http://localhost:3000}")
    private String magicLinkBaseUrl;

    public String requestMagicLink(String email) {
        String normalized = email == null ? "" : email.trim().toLowerCase();
        if (normalized.isBlank() || !normalized.matches(EMAIL_REGEX)) {
            throw new IllegalArgumentException("A valid email is required");
        }

        Instant now = Instant.now();
        long recentRequests = magicLinkTokenRepository.countByEmailAndCreatedAtAfter(
                normalized, now.minusSeconds(3600));
        if (recentRequests >= MAX_MAGIC_LINKS_PER_HOUR) {
            throw new RateLimitExceededException("Too many sign-in links requested. Try again later.");
        }

        String token = generateToken();
        MagicLinkToken stored = new MagicLinkToken();
        stored.setEmail(normalized);
        stored.setTokenHash(hashToken(token));
        stored.setExpiresAt(now.plusSeconds(magicLinkExpirationMinutes * 60));
        magicLinkTokenRepository.save(stored);

        String url = magicLinkBaseUrl + "/auth/callback?token=" + token;
        emailService.sendMagicLink(stored.getEmail(), url);

        return "If an account exists with " + normalized + ", a magic link has been sent.";
    }

    @Transactional
    public AuthResponse verifyMagicLink(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Token is required");
        }

        MagicLinkToken stored = magicLinkTokenRepository.findByTokenHashForUpdate(hashToken(token))
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired magic link"));

        if (stored.getUsedAt() != null) {
            throw new IllegalArgumentException("This magic link has already been used");
        }
        if (stored.getExpiresAt().isBefore(Instant.now())) {
            throw new IllegalArgumentException("This magic link has expired");
        }

        stored.setUsedAt(Instant.now());
        String name = stored.getEmail().split("@")[0];
        User user = findOrCreate(stored.getEmail(), name, "EMAIL", null);
        String jwt = jwtTokenProvider.generateToken(user);
        return new AuthResponse(jwt, new AuthResponse.UserDto(user.getId(), user.getEmail(), user.getName()));
    }

    public AuthResponse googleLogin(String code, String redirectUri) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Authorization code is required");
        }
        if (redirectUri == null || redirectUri.isBlank()) {
            throw new IllegalArgumentException("Redirect URI is required");
        }

        GoogleOAuthService.GoogleUserInfo info = googleOAuthService.getUserInfo(code, redirectUri);
        User user = findOrCreate(info.email(), info.name(), "GOOGLE", info.id());
        String jwt = jwtTokenProvider.generateToken(user);
        return new AuthResponse(jwt, new AuthResponse.UserDto(user.getId(), user.getEmail(), user.getName()));
    }

    private User findOrCreate(String email, String name, String provider, String providerId) {
        String normalizedEmail = email.trim().toLowerCase();
        String normalizedName = name == null || name.isBlank()
                ? normalizedEmail.split("@")[0]
                : name.trim();
        return userRepository.findByEmail(normalizedEmail)
                .map(existing -> {
                    existing.setName(normalizedName);
                    existing.setAuthProvider(provider);
                    existing.setAuthProviderId(providerId);
                    return userRepository.save(existing);
                })
                .orElseGet(() -> {
                    User user = new User();
                    user.setEmail(normalizedEmail);
                    user.setName(normalizedName);
                    user.setAuthProvider(provider);
                    user.setAuthProviderId(providerId);
                    return userRepository.save(user);
                });
    }

    private String generateToken() {
        byte[] bytes = new byte[TOKEN_BYTES];
        RANDOM.nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }
}
