package org.chzz.market.domain.user.dto.response;

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
