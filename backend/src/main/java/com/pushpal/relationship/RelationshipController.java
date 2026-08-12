package com.pushpal.relationship;

import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
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
        UserRelationship relationship = relationshipService.createInvite(userId);
        return ResponseEntity.ok(Map.of("inviteCode", relationship.getInviteCode()));
    }

    @PostMapping("/accept")
    public ResponseEntity<Map<String, String>> acceptInvite(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody AcceptInviteRequest request) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        relationshipService.acceptInvite(request.inviteCode(), userId);
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

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> removePal(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID id) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        relationshipService.removePal(id, userId);
        return ResponseEntity.ok(Map.of("message", "Pal removed and pending reminders cancelled"));
    }
}
