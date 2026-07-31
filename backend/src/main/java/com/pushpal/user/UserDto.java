package com.pushpal.user;

import java.util.UUID;

public record UserDto(UUID id, String email, String name) {

    public static UserDto fromEntity(User user) {
        return new UserDto(user.getId(), user.getEmail(), user.getName());
    }
}
