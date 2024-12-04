package org.chzz.market.domain.bid.dto.response;

import lombok.Getter;
import org.chzz.market.domain.auction.dto.response.BaseAuctionResponse;

@Getter
public class BiddingRecord extends BaseAuctionResponse {
    private final Long timeRemaining;
    private final Long participantCount;
    private final Long bidAmount;

    public BiddingRecord(Long auctionId, String auctionName, String imageUrl, Long minPrice, Boolean isSeller,
                         Long timeRemaining, Long participantCount, Long bidAmount) {
        super(auctionId, auctionName, imageUrl, minPrice, isSeller);
        this.timeRemaining = timeRemaining;
        this.participantCount = participantCount;
        this.bidAmount = bidAmount;
    }
}

