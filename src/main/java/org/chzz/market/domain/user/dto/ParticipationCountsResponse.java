package org.chzz.market.domain.user.dto;

import com.querydsl.core.annotations.QueryProjection;

public record ParticipationCountsResponse(
        long preRegistrationCount,
        long successfulBidCount,
        long failedBidCount,
        long endedAuctionCount
) {
    @QueryProjection
    public ParticipationCountsResponse {}
}
