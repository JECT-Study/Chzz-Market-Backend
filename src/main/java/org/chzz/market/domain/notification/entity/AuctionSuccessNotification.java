package org.chzz.market.domain.notification.entity;

import static org.chzz.market.domain.notification.entity.NotificationType.Values.AUCTION_SUCCESS;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.NoArgsConstructor;
import org.chzz.market.domain.image.entity.Image;
import org.chzz.market.domain.user.entity.User;

@Entity
@NoArgsConstructor
@DiscriminatorValue(value = AUCTION_SUCCESS)
public class AuctionSuccessNotification extends Notification {
    private Long auctionId;

    public AuctionSuccessNotification(User user, Image image, String message, Long auctionId) {
        super(user, image, message);
        this.auctionId = auctionId;
    }
}
