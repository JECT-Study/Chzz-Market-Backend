package org.chzz.market.domain.auction.dto.event;

import org.chzz.market.domain.auction.entity.Auction;

public record AuctionStartEvent(Auction auction) {
}