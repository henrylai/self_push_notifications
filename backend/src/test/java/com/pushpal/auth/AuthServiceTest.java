package com.pushpal.auth;

import com.pushpal.common.RateLimitExceededException;
import com.pushpal.user.User;
import com.pushpal.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private MagicLinkTokenRepository magicLinkTokenRepository;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private EmailService emailService;

    @Mock
    private GoogleOAuthService googleOAuthService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(
                userRepository, magicLinkTokenRepository, jwtTokenProvider, emailService, googleOAuthService);
        ReflectionTestUtils.setField(authService, "magicLinkExpirationMinutes", 15L);
        ReflectionTestUtils.setField(authService, "magicLinkBaseUrl", "https://pushpal.up.railway.app");
    }

    @Test
    void requestMagicLinkRejectsInvalidEmail() {
        assertThatThrownBy(() -> authService.requestMagicLink("not-an-email"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("valid email");
    }

    @Test
    void requestMagicLinkStoresHashedTokenAndEmailsLink() {
        ArgumentCaptor<MagicLinkToken> captor = ArgumentCaptor.forClass(MagicLinkToken.class);
        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);

        authService.requestMagicLink("  User@Example.com ");

        verify(magicLinkTokenRepository).save(captor.capture());
        verify(emailService).sendMagicLink(eq("user@example.com"), urlCaptor.capture());

        MagicLinkToken stored = captor.getValue();
        assertThat(stored.getEmail()).isEqualTo("user@example.com");
        assertThat(stored.getUsedAt()).isNull();
        assertThat(stored.getExpiresAt()).isAfter(Instant.now());

        String url = urlCaptor.getValue();
        String token = url.substring(url.lastIndexOf('=') + 1);
        assertThat(stored.getTokenHash()).isEqualTo(sha256(token));
        assertThat(url).startsWith("https://pushpal.up.railway.app/auth/callback?token=");
    }

    @Test
    void requestMagicLinkReturnsGenericMessage() {
        when(magicLinkTokenRepository.save(any(MagicLinkToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        String message = authService.requestMagicLink("user@example.com");
        assertThat(message).contains("If an account exists with user@example.com");
    }

    @Test
    void requestMagicLinkIsRateLimitedPerEmail() {
        when(magicLinkTokenRepository.countByEmailAndCreatedAtAfter(
                eq("user@example.com"), any(Instant.class))).thenReturn(5L);

        assertThatThrownBy(() -> authService.requestMagicLink("user@example.com"))
                .isInstanceOf(RateLimitExceededException.class)
                .hasMessageContaining("Too many sign-in links");
    }

    @Test
    void verifyMagicLinkCreatesUserAndMarksTokenUsed() {
        User user = newUser("user@example.com", "user");
        String token = "valid-token";
        MagicLinkToken stored = newToken("user@example.com", token, Instant.now().plusSeconds(900));

        when(magicLinkTokenRepository.consumeValidToken(eq(sha256(token)), any(Instant.class)))
                .thenReturn(1);
        when(magicLinkTokenRepository.findByTokenHash(sha256(token)))
                .thenReturn(Optional.of(stored));
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(jwtTokenProvider.generateToken(user)).thenReturn("jwt-token");

        AuthResponse response = authService.verifyMagicLink(token);

        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(response.user().email()).isEqualTo("user@example.com");
        verify(magicLinkTokenRepository).consumeValidToken(
                eq(sha256(token)), any(Instant.class));
    }

    @Test
    void verifyMagicLinkRejectsUnknownToken() {
        when(magicLinkTokenRepository.consumeValidToken(any(), any(Instant.class))).thenReturn(0);

        assertThatThrownBy(() -> authService.verifyMagicLink("unknown"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid or expired");
    }

    @Test
    void verifyMagicLinkRejectsUsedToken() {
        MagicLinkToken stored = newToken("user@example.com", "used-token", Instant.now().plusSeconds(900));
        stored.setUsedAt(Instant.now().minusSeconds(60));
        when(magicLinkTokenRepository.consumeValidToken(
                eq(sha256("used-token")), any(Instant.class))).thenReturn(0);

        assertThatThrownBy(() -> authService.verifyMagicLink("used-token"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid or expired");
    }

    @Test
    void verifyMagicLinkRejectsExpiredToken() {
        MagicLinkToken stored = newToken("user@example.com", "expired-token", Instant.now().minusSeconds(60));
        when(magicLinkTokenRepository.consumeValidToken(
                eq(sha256("expired-token")), any(Instant.class))).thenReturn(0);

        assertThatThrownBy(() -> authService.verifyMagicLink("expired-token"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid or expired");
    }

    @Test
    void googleLoginThrowsWhenNotConfigured() {
        when(googleOAuthService.getUserInfo(any(), any()))
                .thenThrow(new IllegalStateException("Google login is not configured"));

        assertThatThrownBy(() -> authService.googleLogin("code", "https://app/auth/callback"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not configured");
    }

    @Test
    void googleLoginCreatesUserAndReturnsJwt() {
        User user = newUser("user@gmail.com", "John Doe");
        when(googleOAuthService.getUserInfo("code", "https://app/auth/callback"))
                .thenReturn(new GoogleOAuthService.GoogleUserInfo(
                        "google-id", "user@gmail.com", "John Doe", null));
        when(userRepository.findByEmail("user@gmail.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(jwtTokenProvider.generateToken(user)).thenReturn("jwt-token");

        AuthResponse response = authService.googleLogin("code", "https://app/auth/callback");

        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(response.user().email()).isEqualTo("user@gmail.com");
    }

    @Test
    void googleLoginRejectsMissingArguments() {
        assertThatThrownBy(() -> authService.googleLogin("", "https://app/auth/callback"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> authService.googleLogin("code", ""))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private User newUser(String email, String name) {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail(email);
        user.setName(name);
        user.setAuthProvider("EMAIL");
        return user;
    }

    private MagicLinkToken newToken(String email, String rawToken, Instant expiresAt) {
        MagicLinkToken token = new MagicLinkToken();
        token.setEmail(email);
        token.setTokenHash(sha256(rawToken));
        token.setExpiresAt(expiresAt);
        return token;
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
