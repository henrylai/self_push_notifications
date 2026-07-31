package com.pushpal.auth;

import com.pushpal.user.User;
import com.pushpal.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    public String requestMagicLink(String email) {
        String token = generateInviteCode();
        String hashedToken = passwordEncoder.encode(token);

        log.info("Magic link requested for email: {} — token: {} (hash stored)", email, token);

        // In production: send email with magic link containing the token
        return "If an account exists with " + email + ", a magic link has been sent.";
    }

    public AuthResponse verifyMagicLink(String token) {
        // In production: look up token hash from store, validate expiry
        // For now, accept any token and create/return a user
        log.info("Verifying magic link token");

        // This is a simplified flow — in production you'd look up the token
        throw new UnsupportedOperationException(
                "Magic link verification requires a persistent token store. Use Google login for now.");
    }

    public AuthResponse googleLogin(String idToken) {
        // In production: validate idToken with Google's API
        // For now, extract a placeholder user
        log.info("Google login with token: {}...", idToken.substring(0, Math.min(20, idToken.length())));

        String email = "user-" + UUID.randomUUID() + "@google.placeholder";
        String name = "Google User";

        User user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setEmail(email);
                    newUser.setName(name);
                    newUser.setAuthProvider("GOOGLE");
                    newUser.setAuthProviderId(idToken);
                    newUser.setCreatedAt(Instant.now());
                    newUser.setUpdatedAt(Instant.now());
                    return userRepository.save(newUser);
                });

        String jwt = jwtTokenProvider.generateToken(user);
        return new AuthResponse(jwt, new AuthResponse.UserDto(user.getId(), user.getEmail(), user.getName()));
    }

    public String generateInviteCode() {
        StringBuilder code = new StringBuilder(6);
        for (int i = 0; i < 6; i++) {
            code.append(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length())));
        }
        return code.toString();
    }
}
