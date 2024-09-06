package org.chzz.market.domain.notification.dto;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.chzz.market.domain.image.entity.Image;
import org.chzz.market.domain.notification.entity.NotificationType;

@Getter
@NoArgsConstructor
public class NotificationMessage {
    private List<Long> userIds = new ArrayList<>();
    private NotificationType type;
    private String message;
    private Image image;

    public NotificationMessage(List<Long> userIds, NotificationType type, String message, Image image) {
        this.userIds = userIds;
        this.type = type;
        this.message = message;
        this.image = image;
    }

    public NotificationMessage(Long userId, NotificationType type, String message, Image image) {
        this.userIds.add(userId);
        this.type = type;
        this.message = message;
        this.image = image;
    }
}
