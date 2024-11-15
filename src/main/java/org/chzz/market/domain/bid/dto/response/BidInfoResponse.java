package org.chzz.market.domain.bid.dto.response;

import com.querydsl.core.annotations.QueryProjection;

public record BidInfoResponse(Long bidAmount, String bidderNickname, Boolean isWinningBidder) {
    @QueryProjection
    public BidInfoResponse {
    }
}
