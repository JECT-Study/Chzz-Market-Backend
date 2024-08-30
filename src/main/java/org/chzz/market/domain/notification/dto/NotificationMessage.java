package org.chzz.market.domain.notification.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.chzz.market.domain.notification.entity.NotificationType;
import org.chzz.market.domain.product.entity.Product;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationMessage {
    private List<Long> userIds = new ArrayList<>();
    private String message;
    private NotificationType type;

    @JsonIgnore
    private Product product;

    public NotificationMessage(List<Long> userIds, NotificationType type, Product product) {
        this.userIds = userIds;
        this.type = type;
        this.product = product;
        this.message = type.getMessage(product.getName());
    }

    public NotificationMessage(Long userId, NotificationType type, Product product) {
        this.userIds.add(userId);
        this.type = type;
        this.product = product;
        this.message = type.getMessage(product.getName());
    }

    public NotificationMessage(List<Long> userIds, NotificationType type,
                               String productName) { // TODO: 사전등록 취소 알림을 위해 임시 작성 (소프트 딜리트로 변경시 삭제)
        this.userIds = userIds;
        this.type = type;
        this.message = type.getMessage(productName);
    }

    public NotificationMessage(Long userId, NotificationType type,
                               String productName) { // TODO: 사전등록 취소 알림을 위해 임시 작성 (소프트 딜리트로 변경시 삭제)
        this.userIds.add(userId);
        this.type = type;
        this.message = type.getMessage(productName);
    }
}
