package com.ic.api.notification.dto;

import lombok.Builder;

@Builder
public record NotificationSummaryResponse(
        long unreadCount,
        long totalCount
) {

    public static NotificationSummaryResponse of(long unreadCount, long totalCount) {
        return NotificationSummaryResponse.builder()
                .unreadCount(unreadCount)
                .totalCount(totalCount)
                .build();
    }
}