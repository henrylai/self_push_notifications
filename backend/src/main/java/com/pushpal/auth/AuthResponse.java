package com.pushpal.auth;

import java.util.UUID;

public record AuthResponse(String token, UserDto user) {
    public record UserDto(UUID id, String email, String name) {}
}
