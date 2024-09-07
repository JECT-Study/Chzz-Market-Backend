package org.chzz.market.domain.auction.dto.response;

import com.querydsl.core.annotations.QueryProjection;

import java.time.LocalDateTime;

public record WonAuctionResponse (
        Long id,
        String name,
        String cdnPath,
        Integer minPrice,
        LocalDateTime endDateTime,
        Long winningBid
) {
    @QueryProjection
    public WonAuctionResponse {}
}
