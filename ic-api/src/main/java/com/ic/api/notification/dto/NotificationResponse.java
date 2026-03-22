package com.ic.api.notification.dto;

import com.ic.domain.notification.Notification;
import com.ic.domain.notification.NotificationType;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record NotificationResponse(
        Long id,
        NotificationType type,
        String title,
        String content,
        Long referenceId,
        String referenceType,
        boolean isRead,
        LocalDateTime createdAt
) {

    public static NotificationResponse from(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .type(notification.getType())
                .title(notification.getTitle())
                .content(notification.getContent())
                .referenceId(notification.getReferenceId())
                .referenceType(notification.getReferenceType())
                .isRead(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}