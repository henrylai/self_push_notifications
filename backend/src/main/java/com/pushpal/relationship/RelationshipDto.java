package com.pushpal.relationship;

import com.pushpal.user.User;

import java.util.UUID;

public record RelationshipDto(
        UUID id,
        UUID palId,
        String palName,
        String palEmail,
        String status,
        @Deprecated UUID partnerId,
        @Deprecated String partnerName,
        @Deprecated String partnerEmail
) {
    public static RelationshipDto fromEntity(UserRelationship relationship, UUID currentUserId) {
        User pal = null;
        if (currentUserId.equals(relationship.getInviter().getId())) {
            pal = relationship.getInvitee();
        } else if (relationship.getInvitee() != null
                && currentUserId.equals(relationship.getInvitee().getId())) {
            pal = relationship.getInviter();
        }

        return new RelationshipDto(
                relationship.getId(),
                pal != null ? pal.getId() : null,
                pal != null ? pal.getName() : null,
                pal != null ? pal.getEmail() : null,
                relationship.getStatus(),
                pal != null ? pal.getId() : null,
                pal != null ? pal.getName() : null,
                pal != null ? pal.getEmail() : null);
    }
}
