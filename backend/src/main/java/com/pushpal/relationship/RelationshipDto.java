package com.pushpal.relationship;

import com.pushpal.user.User;

import java.util.UUID;

public record RelationshipDto(
        UUID id,
        UUID partnerId,
        String partnerName,
        String partnerEmail,
        String status
) {
    public static RelationshipDto fromEntity(UserRelationship relationship, UUID currentUserId) {
        User partner = null;
        if (currentUserId.equals(relationship.getInviter().getId())) {
            partner = relationship.getInvitee();
        } else if (relationship.getInvitee() != null
                && currentUserId.equals(relationship.getInvitee().getId())) {
            partner = relationship.getInviter();
        }

        return new RelationshipDto(
                relationship.getId(),
                partner != null ? partner.getId() : null,
                partner != null ? partner.getName() : null,
                partner != null ? partner.getEmail() : null,
                relationship.getStatus());
    }
}
