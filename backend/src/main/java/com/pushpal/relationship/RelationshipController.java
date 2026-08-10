package com.pushpal.relationship;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/relationships")
@RequiredArgsConstructor
public class RelationshipController {

    private final RelationshipService relationshipService;

    @PostMapping("/invite")
    public ResponseEntity<Map<String, String>> createInvite(@AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        String inviteCode = relationshipService.generateInviteCode();
        relationshipService.createInvite(userId, inviteCode);
        return ResponseEntity.ok(Map.of("inviteCode", inviteCode));
    }

    @PostMapping("/accept")
    public ResponseEntity<Map<String, String>> acceptInvite(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, String> request) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        String inviteCode = request.get("inviteCode");
        if (inviteCode == null || inviteCode.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invite code is required"));
        }
        relationshipService.acceptInvite(inviteCode, userId);
        return ResponseEntity.ok(Map.of("message", "Invite accepted successfully"));
    }

    @GetMapping
    public ResponseEntity<List<RelationshipDto>> listRelationships(
            @AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        List<RelationshipDto> relationships = relationshipService.getRelationshipsForUser(userId)
                .stream()
                .map(r -> RelationshipDto.fromEntity(r, userId))
                .toList();
        return ResponseEntity.ok(relationships);
    }
}
