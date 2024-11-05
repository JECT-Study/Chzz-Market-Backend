package org.chzz.market.domain.notification.entity;

import static org.chzz.market.domain.notification.entity.NotificationType.Values.AUCTION_NON_WINNER;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.NoArgsConstructor;
import org.chzz.market.domain.image.entity.Image;
import org.chzz.market.domain.user.entity.User;

@Entity
@NoArgsConstructor
@DiscriminatorValue(value = AUCTION_NON_WINNER)
public class AuctionNonWinnerNotification extends Notification {

    public AuctionNonWinnerNotification(Long userId, String cdnPath, String message) {
        super(userId, cdnPath, message);
    }
}
