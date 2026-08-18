package com.pushpal.notification;

import com.pushpal.user.User;
import com.pushpal.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class NotificationDtoMapper {

    private final UserRepository userRepository;

    public NotificationDto map(Notification notification) {
        return mapAll(List.of(notification)).getFirst();
    }

    public List<NotificationDto> mapAll(List<Notification> notifications) {
        var userIds = new HashSet<UUID>();
        for (Notification notification : notifications) {
            if (notification.getSenderId() != null) {
                userIds.add(notification.getSenderId());
            }
            userIds.add(notification.getRecipientId());
        }
        Map<UUID, User> users = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));
        return notifications.stream()
                .map(notification -> NotificationDto.fromEntity(
                        notification,
                        nameOf(users, notification.getSenderId()),
                        nameOf(users, notification.getRecipientId())))
                .toList();
    }

    private String nameOf(Map<UUID, User> users, UUID userId) {
        User user = userId == null ? null : users.get(userId);
        return user == null ? null : user.getName();
    }
}
