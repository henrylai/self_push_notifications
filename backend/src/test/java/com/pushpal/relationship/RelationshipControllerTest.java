package com.pushpal.relationship;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RelationshipControllerTest {

    @Mock
    private RelationshipService relationshipService;

    private RelationshipController controller;

    @BeforeEach
    void setUp() {
        controller = new RelationshipController(relationshipService);
    }

    @Test
    void removesPalForAuthenticatedUser() {
        UUID userId = UUID.randomUUID();
        UUID relationshipId = UUID.randomUUID();

        ResponseEntity<Map<String, String>> response = controller.removePal(
                userDetails(userId), relationshipId);

        verify(relationshipService).removePal(relationshipId, userId);
        assertThat(response.getBody()).containsEntry(
                "message", "Pal removed and pending reminders cancelled");
    }

    private UserDetails userDetails(UUID userId) {
        return org.springframework.security.core.userdetails.User.withUsername(userId.toString())
                .password("unused")
                .authorities("USER")
                .build();
    }
}
