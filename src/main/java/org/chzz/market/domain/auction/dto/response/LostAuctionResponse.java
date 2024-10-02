package org.chzz.market.domain.auction.dto.response;

import com.querydsl.core.annotations.QueryProjection;

import java.time.LocalDateTime;

public record LostAuctionResponse (
        Long auctionId,
        String productName,
        String imageUrl,
        Integer minPrice,
        LocalDateTime endDateTime,
        Long highestAmount
) {
    @QueryProjection
    public LostAuctionResponse {}
}
