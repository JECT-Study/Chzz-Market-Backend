package org.chzz.market.domain.notification.dto;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.chzz.market.domain.notification.entity.Notification.Type;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationMessage {
    private List<Long> userIds = new ArrayList<>();
    private String message;
    private Type type;

    public NotificationMessage(List<Long> userIds, Type type, String productName) {
        this.userIds = userIds;
        this.type = type;
        this.message = type.getMessage(productName);
    }

    public NotificationMessage(Long userId, Type type, String productName) {
        this.userIds.add(userId);
        this.type = type;
        this.message = type.getMessage(productName);
    }
}