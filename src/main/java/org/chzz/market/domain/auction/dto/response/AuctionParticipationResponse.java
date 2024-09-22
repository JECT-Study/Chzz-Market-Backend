package org.chzz.market.domain.auction.dto.response;

import com.querydsl.core.annotations.QueryProjection;
import org.chzz.market.domain.auction.type.AuctionStatus;

public record AuctionParticipationResponse (
        AuctionStatus status,
        Long winnerId,
        Long count
) {
    @QueryProjection
    public AuctionParticipationResponse {}
}
