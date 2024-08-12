package org.chzz.market.domain.auction.dto;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public abstract class BaseAuctionDTO {
    protected static final long AUCTION_DURATION_HOURS = 24;

    protected String name;
    protected String cdnPath;
    protected Long timeRemaining;
    protected Long minPrice;
    protected Long participantCount;

    public BaseAuctionDTO(String name, String cdnPath, Long timeRemaining, Long minPrice, Long participantCount) {
        this.name = name;
        this.cdnPath = cdnPath;
        this.timeRemaining = timeRemaining;
        this.minPrice = minPrice;
        this.participantCount = participantCount;
    }
}