package org.chzz.market.domain.notification.event;

import java.util.List;
import org.chzz.market.domain.image.entity.Image;
import org.chzz.market.domain.notification.entity.NotificationType;

public record NotificationEvent(List<Long> userIds, NotificationType type, String message, Image image) {

    // 단일 사용자를 위한 정적 메서드
    public static NotificationEvent of(Long userId, NotificationType type, String message, Image image) {
        return new NotificationEvent(List.of(userId), type, message, image);
    }

    // 여러 사용자를 위한 정적 메서드
    public static NotificationEvent of(List<Long> userIds, NotificationType type, String message, Image image) {
        return new NotificationEvent(userIds, type, message, image);
    }
}
