package org.chzz.market.domain.bid.dto.query;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;
import org.chzz.market.domain.auction.dto.BaseAuctionDto;
@Getter
public class BiddingRecord extends BaseAuctionDto {
    private final Long auctionId;
    private final Long bidAmount;

    @QueryProjection
    public BiddingRecord(Long auctionId, String productName, Long minPrice, Long bidAmount, Long participantCount, String cdnPath,
                         Long timeRemaining) {
        super(productName, cdnPath, timeRemaining, minPrice, participantCount);
        this.auctionId = auctionId;
        this.bidAmount = bidAmount;
    }
}
