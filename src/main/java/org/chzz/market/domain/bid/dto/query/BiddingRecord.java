package org.chzz.market.domain.bid.dto.query;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;
import lombok.ToString;
import org.chzz.market.domain.auction.dto.BaseAuctionDTO;
@Getter
@ToString(callSuper = true)
public class BiddingRecord extends BaseAuctionDTO {
    private final Long bidAmount;

    @QueryProjection
    public BiddingRecord(String productName, Long minPrice, Long bidAmount, Long participantCount, String cdnPath,
                         Long timeRemaining) {
        super(productName, cdnPath, timeRemaining, minPrice, participantCount);
        this.bidAmount = bidAmount;
    }
}