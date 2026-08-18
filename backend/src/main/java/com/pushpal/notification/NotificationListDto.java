package com.pushpal.notification;

import java.util.List;

public record NotificationListDto(
        List<NotificationDto> received,
        List<NotificationDto> sent,
        int page,
        int size,
        boolean receivedHasMore,
        boolean sentHasMore
) {}
