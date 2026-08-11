package com.pushpal.relationship;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RelationshipRepository extends JpaRepository<UserRelationship, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT relationship FROM UserRelationship relationship "
            + "WHERE relationship.inviteCode = :inviteCode")
    Optional<UserRelationship> findByInviteCodeForUpdate(@Param("inviteCode") String inviteCode);

    boolean existsByInviteCode(String inviteCode);

    @EntityGraph(attributePaths = {"inviter", "invitee"})
    List<UserRelationship> findByInviterIdAndStatus(UUID inviterId, String status);

    @EntityGraph(attributePaths = {"inviter", "invitee"})
    List<UserRelationship> findByInviteeIdAndStatus(UUID inviteeId, String status);

    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM UserRelationship r "
            + "WHERE r.status = 'ACCEPTED' AND "
            + "((r.inviter.id = :firstUserId AND r.invitee.id = :secondUserId) OR "
            + "(r.inviter.id = :secondUserId AND r.invitee.id = :firstUserId))")
    boolean areUsersLinked(@Param("firstUserId") UUID firstUserId,
                           @Param("secondUserId") UUID secondUserId);
}
