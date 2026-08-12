package com.pushpal.relationship;

import com.pushpal.user.User;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class RelationshipDtoTest {

    @Test
    void returnsPalFieldsForTheOtherUser() {
        User currentUser = user("Current");
        User pal = user("Pal");
        UserRelationship relationship = new UserRelationship();
        relationship.setId(UUID.randomUUID());
        relationship.setInviter(currentUser);
        relationship.setInvitee(pal);
        relationship.setStatus("ACCEPTED");

        RelationshipDto dto = RelationshipDto.fromEntity(relationship, currentUser.getId());

        assertThat(dto.palId()).isEqualTo(pal.getId());
        assertThat(dto.palName()).isEqualTo("Pal");
        assertThat(dto.palEmail()).isEqualTo(pal.getEmail());
        assertThat(dto.partnerId()).isEqualTo(pal.getId());
    }

    private User user(String name) {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setName(name);
        user.setEmail(name.toLowerCase() + "@example.com");
        return user;
    }
}
