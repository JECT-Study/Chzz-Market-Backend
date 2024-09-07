package org.chzz.market.domain.bid.dto.response;

import com.querydsl.core.annotations.QueryProjection;

public record BidInfoResponse(Long amount, String nickname, Boolean isWinningBidder) {
    @QueryProjection
    public BidInfoResponse {
    }
}
