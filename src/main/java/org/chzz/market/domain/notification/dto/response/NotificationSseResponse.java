package org.chzz.market.domain.notification.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.chzz.market.domain.notification.dto.NotificationRealMessage;
import org.chzz.market.domain.notification.entity.NotificationType;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record NotificationSseResponse(
        Long notificationId,
        String message,
        NotificationType type,
        Long auctionId
) {
    public static NotificationSseResponse of(NotificationRealMessage notificationRealMessage, Long notificationId) {
        return new NotificationSseResponse(
                notificationId,
                notificationRealMessage.message(),
                notificationRealMessage.type(),
                notificationRealMessage.auctionId()
        );
    }

}
