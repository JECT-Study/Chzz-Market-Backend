package org.chzz.market.domain.notification.event;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import lombok.ToString;
import org.chzz.market.domain.image.entity.Image;
import org.chzz.market.domain.notification.entity.Notification;
import org.chzz.market.domain.notification.entity.NotificationType;
import org.chzz.market.domain.user.entity.User;

public record NotificationEvent(List<Long> userIds, NotificationType type, String message, Image image, // 공통 필드
                                Map<String, Object> additionalFields// 동적 필드를 위한 필드
) {
    public static final String FIELD_AUCTION_ID = "auctionId"; // 추가로 필요한 필드 이름
    private static final Map<String, Object> EMPTY_FIELDS = Collections.emptyMap(); // 미리 빈 Map을 초기화

    // 경매 ID가 필요한 단일 사용자를 위한 정적 메서드
    public static NotificationEvent createAuctionNotification(Long userId, NotificationType type, String message,
                                                              Image image, Long auctionId) {
        return new NotificationEvent(List.of(userId), type, message, image, Map.of(FIELD_AUCTION_ID, auctionId));
    }

    // 경매 ID가 필요한 여러 사용자를 위한 정적 메서드
    public static NotificationEvent createAuctionNotification(List<Long> userIds, NotificationType type, String message,
                                                              Image image, Long auctionId) {
        return new NotificationEvent(userIds, type, message, image, Map.of(FIELD_AUCTION_ID, auctionId));
    }

    // 경매 ID가 필요 없는 단일 사용자를 위한 정적 메서드
    public static NotificationEvent createSimpleNotification(Long userId, NotificationType type, String message,
                                                             Image image) {
        return new NotificationEvent(List.of(userId), type, message, image, EMPTY_FIELDS);
    }

    // 경매 ID가 필요 없는 여러 사용자를 위한 정적 메서드
    public static NotificationEvent createSimpleNotification(List<Long> userIds, NotificationType type, String message,
                                                             Image image) {
        return new NotificationEvent(userIds, type, message, image, EMPTY_FIELDS);
    }

    public Notification toEntity(User user) {
        return type.createNotification(user, this);
    }

    public Long getAuctionId() {
        return getFieldValue(FIELD_AUCTION_ID, Long.class);
    }

    // 헬퍼 메서드: 동적 필드에서 값을 안전하게 가져옴
    private <T> T getFieldValue(String fieldKey, Class<T> type) {
        Object value = additionalFields.get(fieldKey);
        if (type.isInstance(value)) {
            return type.cast(value);
        }
        return null;
    }

    @Override
    public String toString() {
        return String.format(
                "NotificationEvent[userIds=%s, type=%s, message=%s, additionalFields=%s]",
                userIds, type, message, additionalFields
        );
    }
}
