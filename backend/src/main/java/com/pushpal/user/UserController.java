package com.pushpal.user;

import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserDto> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        User user = userService.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return ResponseEntity.ok(UserDto.fromEntity(user));
    }

    @PutMapping("/me")
    public ResponseEntity<UserDto> updateCurrentUser(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UpdateUserRequest request) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        User user = userService.updateUser(userId, request.name().trim());
        return ResponseEntity.ok(UserDto.fromEntity(user));
    }
}
