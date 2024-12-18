package org.chzz.market.domain.auction.dto.event;

import java.util.Map;
import org.chzz.market.domain.auction.entity.Auction;

public record AuctionImageUpdateEvent(Auction auction,
                                      Map<Long, Integer> imageSequence,
                                      Map<String, String> objectKeyBuffer) {
}
