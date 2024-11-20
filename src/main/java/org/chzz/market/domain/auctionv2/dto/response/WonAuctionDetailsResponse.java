package org.chzz.market.domain.auctionv2.dto.response;

import com.querydsl.core.annotations.QueryProjection;

public record WonAuctionDetailsResponse(
        Long auctionId,
        String productName,
        String imageUrl,
        Long winningAmount
) {
    @QueryProjection
    public WonAuctionDetailsResponse {
    }
}
