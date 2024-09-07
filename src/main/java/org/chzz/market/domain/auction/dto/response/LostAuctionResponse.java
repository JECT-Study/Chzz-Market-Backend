package org.chzz.market.domain.auction.dto.response;

import com.querydsl.core.annotations.QueryProjection;

import java.time.LocalDateTime;

public record LostAuctionResponse (
        Long id,
        String name,
        String cdnPath,
        Integer minPrice,
        LocalDateTime endDateTime,
        Long highestBid
) {
    @QueryProjection
    public LostAuctionResponse {}
}
