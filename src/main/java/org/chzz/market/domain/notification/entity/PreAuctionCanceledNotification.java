package org.chzz.market.domain.notification.entity;

import static org.chzz.market.domain.notification.entity.NotificationType.Values.PRE_AUCTION_CANCELED;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@DiscriminatorValue(value = PRE_AUCTION_CANCELED)
public class PreAuctionCanceledNotification extends Notification {

    public PreAuctionCanceledNotification(Long userId, String cdnPath, String message) {
        super(userId, cdnPath, message);
    }
}
