package org.chzz.market.domain.notification.dto.response;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import java.util.Map;
import org.chzz.market.domain.notification.dto.NotificationRealMessage;
import org.chzz.market.domain.notification.entity.NotificationType;

public record NotificationSseResponse(
        Long notificationId,
        String message,
        NotificationType type,
        @JsonAnyGetter
        Map<String, Object> additionalFields
) {
    public static NotificationSseResponse of(NotificationRealMessage notificationRealMessage, Long notificationId) {
        return new NotificationSseResponse(
                notificationId,
                notificationRealMessage.message(),
                notificationRealMessage.type(),
                notificationRealMessage.additionalFields()
        );
    }

}
