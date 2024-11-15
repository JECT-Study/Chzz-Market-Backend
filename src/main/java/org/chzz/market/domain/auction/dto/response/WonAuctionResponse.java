package org.chzz.market.domain.auction.dto.response;

import com.querydsl.core.annotations.QueryProjection;

import java.time.LocalDateTime;

public record WonAuctionResponse (
        Long auctionId,
        String productName,
        String imageUrl,
        Integer minPrice,
        Long participantCount,
        LocalDateTime endDateTime,
        Long winningAmount,
        Boolean isOrdered,
        Long orderId
) {
    @QueryProjection
    public WonAuctionResponse {}
}
