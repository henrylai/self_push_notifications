package com.pushpal.auth;

import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/magic-link")
    public ResponseEntity<Map<String, String>> requestMagicLink(
            @Valid @RequestBody MagicLinkRequest request) {
        String message = authService.requestMagicLink(request.email());
        return ResponseEntity.ok(Map.of("message", message));
    }

    @PostMapping("/magic-link/verify")
    public ResponseEntity<AuthResponse> verifyMagicLink(
            @Valid @RequestBody MagicLinkVerifyRequest request) {
        AuthResponse response = authService.verifyMagicLink(request.token());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/google")
    public ResponseEntity<AuthResponse> googleLogin(
            @Valid @RequestBody GoogleLoginRequest request) {
        AuthResponse response = authService.googleLogin(request.code(), request.redirectUri());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout() {
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }
}
