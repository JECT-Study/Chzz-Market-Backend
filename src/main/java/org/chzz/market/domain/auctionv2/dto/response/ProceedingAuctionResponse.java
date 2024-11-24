package org.chzz.market.domain.auctionv2.dto.response;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.chzz.market.domain.auctionv2.entity.AuctionStatus;

@Getter
@NoArgsConstructor
public class ProceedingAuctionResponse extends BaseAuctionResponse {
    private Long timeRemaining;
    private AuctionStatus status;
    private Long participantCount;
    private LocalDateTime createdAt;

    public ProceedingAuctionResponse(Long auctionId, String productName, String imageUrl, Long minPrice,
                                     Boolean isSeller,
                                     Long timeRemaining, AuctionStatus status, Long participantCount,
                                     LocalDateTime createdAt) {
        super(auctionId, productName, imageUrl, minPrice, isSeller);
        this.timeRemaining = timeRemaining;
        this.status = status;
        this.participantCount = participantCount;
        this.createdAt = createdAt;
    }
}
