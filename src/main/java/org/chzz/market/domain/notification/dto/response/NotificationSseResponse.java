package org.chzz.market.domain.notification.dto.response;

public record NotificationSseResponse(
        Long notificationId,
        String type,
        String message
) {
}
