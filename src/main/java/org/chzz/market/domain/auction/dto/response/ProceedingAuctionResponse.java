package org.chzz.market.domain.auction.dto.response;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.chzz.market.domain.auction.entity.AuctionStatus;

@Getter
@NoArgsConstructor
public class ProceedingAuctionResponse extends BaseAuctionResponse {
    private Long timeRemaining;
    private AuctionStatus status;
    private Long participantCount;
    private LocalDateTime createdAt;

    public ProceedingAuctionResponse(Long auctionId, String auctionName, String imageUrl, Long minPrice,
                                     Boolean isSeller,
                                     Long timeRemaining, AuctionStatus status, Long participantCount,
                                     LocalDateTime createdAt) {
        super(auctionId, auctionName, imageUrl, minPrice, isSeller);
        this.timeRemaining = timeRemaining;
        this.status = status;
        this.participantCount = participantCount;
        this.createdAt = createdAt;
    }
}
