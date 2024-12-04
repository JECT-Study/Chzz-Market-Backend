package org.chzz.market.domain.auction.dto.response;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class WonAuctionResponse extends BaseAuctionResponse {
    private Long participantCount;
    private LocalDateTime endDateTime;
    private Long winningAmount;
    private Boolean isOrdered;
    private Long orderId;

    public WonAuctionResponse(Long auctionId, String auctionName, String imageUrl, Long minPrice, Boolean isSeller,
                              Long participantCount, LocalDateTime endDateTime, Long winningAmount, Boolean isOrdered,
                              Long orderId) {
        super(auctionId, auctionName, imageUrl, minPrice, isSeller);
        this.participantCount = participantCount;
        this.endDateTime = endDateTime;
        this.winningAmount = winningAmount;
        this.isOrdered = isOrdered;
        this.orderId = orderId;
    }
}
