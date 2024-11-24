package org.chzz.market.domain.auctionv2.dto.response;

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

    public WonAuctionResponse(Long auctionId, String productName, String imageUrl, Long minPrice, Boolean isSeller,
                              Long participantCount, LocalDateTime endDateTime, Long winningAmount, Boolean isOrdered,
                              Long orderId) {
        super(auctionId, productName, imageUrl, minPrice, isSeller);
        this.participantCount = participantCount;
        this.endDateTime = endDateTime;
        this.winningAmount = winningAmount;
        this.isOrdered = isOrdered;
        this.orderId = orderId;
    }
}
