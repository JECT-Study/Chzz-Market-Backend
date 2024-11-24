package org.chzz.market.domain.auctionv2.dto;

import java.time.LocalDateTime;

public record AuctionRegistrationEvent(
        Long auctionId,
        LocalDateTime endDateTime
) {
}
