package org.chzz.market.domain.notification.event;

import java.util.List;
import org.chzz.market.domain.image.entity.Image;
import org.chzz.market.domain.notification.entity.Notification;
import org.chzz.market.domain.notification.entity.NotificationType;
import org.chzz.market.domain.user.entity.User;

public record NotificationEvent(List<Long> userIds, NotificationType type, String message, Image image, Long auctionId) {

    // 경매 ID가 필요한 단일 사용자를 위한 정적 메서드
    public static NotificationEvent of(Long userId, NotificationType type, String message, Image image, Long auctionId) {
        return new NotificationEvent(List.of(userId), type, message, image, auctionId);
    }

    // 경매 ID가 필요한 여러 사용자를 위한 정적 메서드
    public static NotificationEvent of(List<Long> userIds, NotificationType type, String message, Image image, Long auctionId) {
        return new NotificationEvent(userIds, type, message, image, auctionId);
    }

    // 경매 ID가 필요 없는 단일 사용자를 위한 정적 메서드
    public static NotificationEvent of(Long userId, NotificationType type, String message, Image image) {
        return new NotificationEvent(List.of(userId), type, message, image, null);
    }

    // 경매 ID가 필요 없는 여러 사용자를 위한 정적 메서드
    public static NotificationEvent of(List<Long> userIds, NotificationType type, String message, Image image) {
        return new NotificationEvent(userIds, type, message, image, null);
    }

    public Notification toEntity(User user) {
        return type.createNotification(user, this);
    }
}
