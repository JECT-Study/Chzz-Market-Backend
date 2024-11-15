package org.chzz.market.domain.auction.dto;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public abstract class BaseAuctionDto {
    protected String productName;
    protected String imageUrl;
    protected Long timeRemaining;
    protected Long minPrice;
    protected Long participantCount;

    public BaseAuctionDto(String productName, String imageUrl, Long timeRemaining, Long minPrice, Long participantCount) {
        this.productName = productName;
        this.imageUrl = imageUrl;
        this.timeRemaining = timeRemaining;
        this.minPrice = minPrice;
        this.participantCount = participantCount;
    }
}
