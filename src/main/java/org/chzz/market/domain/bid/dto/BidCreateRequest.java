package org.chzz.market.domain.bid.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.chzz.market.domain.auction.entity.Auction;
import org.chzz.market.domain.bid.entity.Bid;
import org.chzz.market.domain.user.entity.User;

@Getter
@AllArgsConstructor
@Builder
public class BidCreateRequest {
    private Long auctionId;
    private Long amount;

    public Bid toEntity(Auction auction, User user) {
        return Bid.builder()
                .auction(auction)
                .bidder(user)
                .amount(amount)
                .build();
    }
}
