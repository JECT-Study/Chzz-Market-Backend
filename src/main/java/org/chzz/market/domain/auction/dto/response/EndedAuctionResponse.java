package org.chzz.market.domain.auction.dto.response;

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

    public EndedAuctionResponse(Long auctionId, String auctionName, String imageUrl, Long minPrice, Boolean isSeller,
                                Long participantCount, Long winningBidAmount, Boolean isWon, Boolean isOrdered,
                                LocalDateTime createAt) {
        super(auctionId, auctionName, imageUrl, minPrice, isSeller);
        this.participantCount = participantCount;
        this.winningBidAmount = winningBidAmount;
        this.isWon = isWon;
        this.isOrdered = isOrdered;
        this.createAt = createAt;
    }
}
