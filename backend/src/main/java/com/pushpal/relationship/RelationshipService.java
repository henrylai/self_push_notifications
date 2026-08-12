package com.pushpal.relationship;

import com.pushpal.notification.NotificationRepository;
import com.pushpal.user.User;
import com.pushpal.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RelationshipService {

    private static final String INVITE_CODE_CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int INVITE_CODE_LENGTH = 6;
    private static final int MAX_CODE_GENERATION_ATTEMPTS = 10;
    private static final long INVITE_EXPIRATION_DAYS = 7;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final RelationshipRepository relationshipRepository;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;

    private String generateInviteCode() {
        StringBuilder code = new StringBuilder(INVITE_CODE_LENGTH);
        for (int i = 0; i < INVITE_CODE_LENGTH; i++) {
            code.append(INVITE_CODE_CHARACTERS.charAt(RANDOM.nextInt(INVITE_CODE_CHARACTERS.length())));
        }
        return code.toString();
    }

    @Transactional
    public UserRelationship createInvite(UUID inviterId) {
        User inviter = userRepository.findById(inviterId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        String inviteCode = null;
        for (int attempt = 0; attempt < MAX_CODE_GENERATION_ATTEMPTS; attempt++) {
            String candidate = generateInviteCode();
            if (!relationshipRepository.existsByInviteCode(candidate)) {
                inviteCode = candidate;
                break;
            }
        }
        if (inviteCode == null) {
            throw new IllegalStateException("Unable to generate an invite code. Try again.");
        }

        UserRelationship relationship = new UserRelationship();
        relationship.setInviter(inviter);
        relationship.setInviteCode(inviteCode);
        relationship.setStatus("PENDING");
        return relationshipRepository.save(relationship);
    }

    @Transactional
    public UserRelationship acceptInvite(String inviteCode, UUID inviteeId) {
        String normalizedCode = inviteCode.trim().toUpperCase(Locale.ROOT);
        UserRelationship relationship = relationshipRepository.findByInviteCodeForUpdate(normalizedCode)
                .orElseThrow(() -> new IllegalArgumentException("Invalid invite code"));

        if (!"PENDING".equals(relationship.getStatus())) {
            throw new IllegalStateException("Invite code has already been used");
        }

        if (relationship.getInviter().getId().equals(inviteeId)) {
            throw new IllegalStateException("Cannot accept your own invite");
        }

        if (relationship.getCreatedAt().plus(INVITE_EXPIRATION_DAYS, ChronoUnit.DAYS)
                .isBefore(Instant.now())) {
            throw new IllegalStateException("Invite code has expired");
        }

        User invitee = userRepository.findById(inviteeId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (relationshipRepository.areUsersLinked(relationship.getInviter().getId(), inviteeId)) {
            throw new IllegalStateException("You are already linked with this Pal");
        }
        relationship.setInvitee(invitee);
        relationship.setStatus("ACCEPTED");
        return relationshipRepository.save(relationship);
    }

    @Transactional(readOnly = true)
    public List<UserRelationship> getRelationshipsForUser(UUID userId) {
        List<UserRelationship> asInviter = new ArrayList<>(
                relationshipRepository.findByInviterIdAndStatus(userId, "ACCEPTED"));
        List<UserRelationship> asInvitee = relationshipRepository.findByInviteeIdAndStatus(
                userId, "ACCEPTED");
        asInviter.addAll(asInvitee);
        return asInviter;
    }

    public boolean areUsersLinked(UUID firstUserId, UUID secondUserId) {
        return relationshipRepository.areUsersLinked(firstUserId, secondUserId);
    }

    @Transactional
    public void removePal(UUID relationshipId, UUID userId) {
        UserRelationship relationship = relationshipRepository.findByIdForUpdate(relationshipId)
                .filter(item -> "ACCEPTED".equals(item.getStatus()))
                .filter(item -> userId.equals(item.getInviter().getId())
                        || (item.getInvitee() != null && userId.equals(item.getInvitee().getId())))
                .orElseThrow(() -> new EntityNotFoundException("Pal not found"));

        UUID palId = userId.equals(relationship.getInviter().getId())
                ? relationship.getInvitee().getId()
                : relationship.getInviter().getId();
        notificationRepository.cancelPendingNotificationsBetweenUsers(userId, palId);
        relationshipRepository.delete(relationship);
    }
}
