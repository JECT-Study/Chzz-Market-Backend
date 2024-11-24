package org.chzz.market.domain.auctionv2.dto.response;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class EndedAuctionResponse extends BaseAuctionResponse {
    private Long participantCount;
    private Long winningBidAmount;
    private Boolean isWon;
    private Boolean isOrdered;
    private LocalDateTime createAt;

    public EndedAuctionResponse(Long auctionId, String productName, String imageUrl, Long minPrice, Boolean isSeller,
                                Long participantCount, Long winningBidAmount, Boolean isWon, Boolean isOrdered,
                                LocalDateTime createAt) {
        super(auctionId, productName, imageUrl, minPrice, isSeller);
        this.participantCount = participantCount;
        this.winningBidAmount = winningBidAmount;
        this.isWon = isWon;
        this.isOrdered = isOrdered;
        this.createAt = createAt;
    }
}