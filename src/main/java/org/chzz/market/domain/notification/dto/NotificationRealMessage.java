package org.chzz.market.domain.notification.dto;

import java.util.Map;
import org.chzz.market.domain.notification.entity.NotificationType;
import org.chzz.market.domain.notification.event.NotificationEvent;

public record NotificationRealMessage(
        Map<Long, Long> notificationIds,
        String message,
        NotificationType type,
        Map<String, Object> additionalFields
) {
    public static NotificationRealMessage of(Map<Long, Long> userNotificationMap,
                                             NotificationEvent notificationEvent) {
        return new NotificationRealMessage(
                userNotificationMap,
                notificationEvent.message(),
                notificationEvent.type(),
                notificationEvent.additionalFields()
        );
    }
}
