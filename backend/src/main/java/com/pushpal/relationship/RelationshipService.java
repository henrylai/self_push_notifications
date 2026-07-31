package com.pushpal.relationship;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RelationshipService {

    private final RelationshipRepository relationshipRepository;

    @Transactional
    public UserRelationship createInvite(UUID inviterId, String inviteCode) {
        UserRelationship relationship = new UserRelationship();
        relationship.setInviter(new com.pushpal.user.User());
        relationship.getInviter().setId(inviterId);
        relationship.setInviteCode(inviteCode);
        relationship.setStatus("PENDING");
        return relationshipRepository.save(relationship);
    }

    @Transactional
    public UserRelationship acceptInvite(String inviteCode, UUID inviteeId) {
        UserRelationship relationship = relationshipRepository.findByInviteCode(inviteCode)
                .orElseThrow(() -> new IllegalArgumentException("Invalid invite code"));

        if (!"PENDING".equals(relationship.getStatus())) {
            throw new IllegalStateException("Invite code has already been used");
        }

        if (relationship.getInviter().getId().equals(inviteeId)) {
            throw new IllegalStateException("Cannot accept your own invite");
        }

        com.pushpal.user.User invitee = new com.pushpal.user.User();
        invitee.setId(inviteeId);
        relationship.setInvitee(invitee);
        relationship.setStatus("ACCEPTED");
        return relationshipRepository.save(relationship);
    }

    public List<UserRelationship> getRelationshipsForUser(UUID userId) {
        List<UserRelationship> asInviter = relationshipRepository.findByInviterId(userId);
        List<UserRelationship> asInvitee = relationshipRepository.findByInviteeId(userId);
        asInviter.addAll(asInvitee);
        return asInviter;
    }
}
