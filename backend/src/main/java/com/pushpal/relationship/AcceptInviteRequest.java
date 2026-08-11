package com.pushpal.relationship;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record AcceptInviteRequest(
        @NotBlank(message = "Invite code is required")
        @Pattern(regexp = "(?i)[A-Z0-9]{6}", message = "Invite code must be 6 letters or numbers")
        String inviteCode
) {}
