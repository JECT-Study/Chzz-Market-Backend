package org.chzz.market.domain.auction.dto.response;

import com.querydsl.core.annotations.QueryProjection;

public record SimpleAuctionResponse (
        String imageUrl,
        String productName,
        Integer minPrice,
        Long participantCount
) {
    @QueryProjection
    public SimpleAuctionResponse {}
}
