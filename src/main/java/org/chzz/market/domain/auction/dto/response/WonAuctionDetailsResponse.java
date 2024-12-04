package org.chzz.market.domain.auction.dto.response;

import com.querydsl.core.annotations.QueryProjection;

public record WonAuctionDetailsResponse(
        Long auctionId,
        String auctionName,
        String imageUrl,
        Long winningAmount
) {
    @QueryProjection
    public WonAuctionDetailsResponse {
    }
}
