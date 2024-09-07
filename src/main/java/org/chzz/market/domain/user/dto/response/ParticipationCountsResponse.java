package org.chzz.market.domain.user.dto.response;

import com.querydsl.core.annotations.QueryProjection;

public record ParticipationCountsResponse(
        long ongoingAuctionCount,
        long successfulAuctionCount,
        long failedAuctionCount,
        long unsuccessfulAuctionCount
) {
    @QueryProjection
    public ParticipationCountsResponse {}
}
