package org.chzz.market.domain.notification.entity;

import static org.chzz.market.domain.notification.entity.NotificationType.Values.AUCTION_FAILURE;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.NoArgsConstructor;
import org.chzz.market.domain.image.entity.Image;
import org.chzz.market.domain.user.entity.User;

@Entity
@NoArgsConstructor
@DiscriminatorValue(value = AUCTION_FAILURE)
public class AuctionFailureNotification extends Notification {

    public AuctionFailureNotification(Long userId, String cdnPath, String message) {
        super(userId, cdnPath, message);
    }
}
