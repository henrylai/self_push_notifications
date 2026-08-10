package com.pushpal.auth;

import com.pushpal.user.User;
import com.pushpal.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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

        String token = generateToken();
        MagicLinkToken stored = new MagicLinkToken();
        stored.setEmail(normalized);
        stored.setTokenHash(hashToken(token));
        stored.setExpiresAt(Instant.now().plusSeconds(magicLinkExpirationMinutes * 60));
        magicLinkTokenRepository.save(stored);

        String url = magicLinkBaseUrl + "/auth/callback?token=" + token;
        emailService.sendMagicLink(stored.getEmail(), url);

        return "If an account exists with " + normalized + ", a magic link has been sent.";
    }

    public AuthResponse verifyMagicLink(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Token is required");
        }

        MagicLinkToken stored = magicLinkTokenRepository.findByTokenHash(hashToken(token))
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired magic link"));

        if (stored.getUsedAt() != null) {
            throw new IllegalArgumentException("This magic link has already been used");
        }
        if (stored.getExpiresAt().isBefore(Instant.now())) {
            throw new IllegalArgumentException("This magic link has expired");
        }

        stored.setUsedAt(Instant.now());
        magicLinkTokenRepository.save(stored);

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
        User user = findOrCreate(info.email(), info.name(), "GOOGLE", info.email());
        String jwt = jwtTokenProvider.generateToken(user);
        return new AuthResponse(jwt, new AuthResponse.UserDto(user.getId(), user.getEmail(), user.getName()));
    }

    private User findOrCreate(String email, String name, String provider, String providerId) {
        return userRepository.findByEmail(email)
                .map(existing -> {
                    existing.setName(name);
                    existing.setAuthProvider(provider);
                    existing.setAuthProviderId(providerId);
                    return userRepository.save(existing);
                })
                .orElseGet(() -> {
                    User user = new User();
                    user.setEmail(email);
                    user.setName(name);
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
