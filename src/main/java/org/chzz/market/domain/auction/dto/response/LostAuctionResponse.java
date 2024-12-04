package org.chzz.market.domain.auction.dto.response;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class LostAuctionResponse extends BaseAuctionResponse {
    private Long participantCount;
    private LocalDateTime endDateTime;
    private Long bidAmount;

    public LostAuctionResponse(Long auctionId, String auctionName, String imageUrl, Long minPrice, Boolean isSeller,
                               Long participantCount, LocalDateTime endDateTime, Long bidAmount) {
        super(auctionId, auctionName, imageUrl, minPrice, isSeller);
        this.participantCount = participantCount;
        this.endDateTime = endDateTime;
        this.bidAmount = bidAmount;
    }
}
