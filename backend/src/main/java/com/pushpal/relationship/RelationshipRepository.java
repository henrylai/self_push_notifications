package com.pushpal.relationship;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RelationshipRepository extends JpaRepository<UserRelationship, UUID> {

    Optional<UserRelationship> findByInviteCode(String inviteCode);

    List<UserRelationship> findByInviterId(UUID inviterId);

    List<UserRelationship> findByInviteeId(UUID inviteeId);
}
