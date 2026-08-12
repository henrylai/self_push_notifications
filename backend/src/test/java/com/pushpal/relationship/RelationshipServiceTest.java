package com.pushpal.relationship;

import com.pushpal.user.User;
import com.pushpal.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RelationshipServiceTest {

    @Mock
    private RelationshipRepository relationshipRepository;

    @Mock
    private UserRepository userRepository;

    private RelationshipService relationshipService;

    @BeforeEach
    void setUp() {
        relationshipService = new RelationshipService(relationshipRepository, userRepository);
    }

    @Test
    void createInviteUsesPersistedInviterAndUniqueCode() {
        User inviter = user();
        when(userRepository.findById(inviter.getId())).thenReturn(Optional.of(inviter));
        when(relationshipRepository.existsByInviteCode(any())).thenReturn(false);
        when(relationshipRepository.save(any(UserRelationship.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UserRelationship result = relationshipService.createInvite(inviter.getId());

        assertThat(result.getInviter()).isSameAs(inviter);
        assertThat(result.getInviteCode()).hasSize(6);
        assertThat(result.getStatus()).isEqualTo("PENDING");
    }

    @Test
    void acceptInviteNormalizesCodeAndLinksPersistedUser() {
        User inviter = user();
        User invitee = user();
        UserRelationship relationship = pendingRelationship(inviter, Instant.now());
        when(relationshipRepository.findByInviteCodeForUpdate("ABC123"))
                .thenReturn(Optional.of(relationship));
        when(userRepository.findById(invitee.getId())).thenReturn(Optional.of(invitee));
        when(relationshipRepository.save(relationship)).thenReturn(relationship);

        UserRelationship result = relationshipService.acceptInvite(" abc123 ", invitee.getId());

        assertThat(result.getInvitee()).isSameAs(invitee);
        assertThat(result.getStatus()).isEqualTo("ACCEPTED");
    }

    @Test
    void expiredInviteCannotBeAccepted() {
        User inviter = user();
        UserRelationship relationship = pendingRelationship(
                inviter, Instant.now().minus(8, ChronoUnit.DAYS));
        when(relationshipRepository.findByInviteCodeForUpdate("ABC123"))
                .thenReturn(Optional.of(relationship));

        assertThatThrownBy(() -> relationshipService.acceptInvite("ABC123", UUID.randomUUID()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("expired");
    }

    @Test
    void relationshipListExcludesPendingInvites() {
        UUID userId = UUID.randomUUID();
        when(relationshipRepository.findByInviterIdAndStatus(userId, "ACCEPTED"))
                .thenReturn(List.of());
        when(relationshipRepository.findByInviteeIdAndStatus(userId, "ACCEPTED"))
                .thenReturn(List.of());

        assertThat(relationshipService.getRelationshipsForUser(userId)).isEmpty();
    }

    @Test
    void relationshipListIncludesEveryAcceptedPal() {
        User currentUser = user();
        User firstPal = user();
        User secondPal = user();
        UserRelationship invited = acceptedRelationship(currentUser, firstPal);
        UserRelationship accepted = acceptedRelationship(secondPal, currentUser);
        when(relationshipRepository.findByInviterIdAndStatus(currentUser.getId(), "ACCEPTED"))
                .thenReturn(List.of(invited));
        when(relationshipRepository.findByInviteeIdAndStatus(currentUser.getId(), "ACCEPTED"))
                .thenReturn(List.of(accepted));

        List<UserRelationship> relationships = relationshipService.getRelationshipsForUser(currentUser.getId());

        assertThat(relationships).containsExactly(invited, accepted);
    }

    @Test
    void cannotAcceptAnotherInviteFromAnAlreadyLinkedPal() {
        User inviter = user();
        User invitee = user();
        UserRelationship relationship = pendingRelationship(inviter, Instant.now());
        when(relationshipRepository.findByInviteCodeForUpdate("ABC123"))
                .thenReturn(Optional.of(relationship));
        when(userRepository.findById(invitee.getId())).thenReturn(Optional.of(invitee));
        when(relationshipRepository.areUsersLinked(inviter.getId(), invitee.getId())).thenReturn(true);

        assertThatThrownBy(() -> relationshipService.acceptInvite("ABC123", invitee.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already linked");
        verify(relationshipRepository).findByInviteCodeForUpdate("ABC123");
    }

    private User user() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail(UUID.randomUUID() + "@example.com");
        user.setName("User");
        return user;
    }

    private UserRelationship pendingRelationship(User inviter, Instant createdAt) {
        UserRelationship relationship = new UserRelationship();
        relationship.setId(UUID.randomUUID());
        relationship.setInviter(inviter);
        relationship.setInviteCode("ABC123");
        relationship.setStatus("PENDING");
        relationship.setCreatedAt(createdAt);
        return relationship;
    }

    private UserRelationship acceptedRelationship(User inviter, User invitee) {
        UserRelationship relationship = pendingRelationship(inviter, Instant.now());
        relationship.setInvitee(invitee);
        relationship.setStatus("ACCEPTED");
        return relationship;
    }
}
