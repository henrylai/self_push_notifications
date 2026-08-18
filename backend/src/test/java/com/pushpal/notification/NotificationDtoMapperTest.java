package com.pushpal.notification;

import com.pushpal.user.User;
import com.pushpal.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationDtoMapperTest {

    @Mock
    private UserRepository userRepository;

    @Test
    void includesSenderAndRecipientNamesInNotificationResponse() {
        User sender = user("Alex");
        User recipient = user("Sam");
        Notification notification = new Notification();
        notification.setSenderId(sender.getId());
        notification.setRecipientId(recipient.getId());
        notification.setIcon(NotificationIcon.BELL);
        when(userRepository.findAllById(any())).thenReturn(List.of(sender, recipient));

        NotificationDto result = new NotificationDtoMapper(userRepository).map(notification);

        assertThat(result.senderName()).isEqualTo("Alex");
        assertThat(result.recipientName()).isEqualTo("Sam");
    }

    private User user(String name) {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setName(name);
        return user;
    }
}
