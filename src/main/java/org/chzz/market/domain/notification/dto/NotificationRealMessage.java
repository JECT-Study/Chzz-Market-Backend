package org.chzz.market.domain.notification.dto;

import java.util.Map;
import org.chzz.market.domain.notification.entity.NotificationType;

public record NotificationRealMessage(
        Map<Long, Long> notificationIds,
        String message,
        NotificationType type
) {
}
