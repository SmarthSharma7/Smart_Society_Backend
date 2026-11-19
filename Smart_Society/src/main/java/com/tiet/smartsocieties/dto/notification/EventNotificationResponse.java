package com.tiet.smartsocieties.dto.notification;

public record EventNotificationResponse(
        Long eventId,
        String eventName,
        String categoryName
) {
}