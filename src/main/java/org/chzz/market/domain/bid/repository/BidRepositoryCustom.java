package org.chzz.market.domain.bid.repository;

import java.util.Optional;
import org.chzz.market.domain.auction.entity.Auction;
import org.chzz.market.domain.bid.dto.query.BiddingRecord;
import org.chzz.market.domain.bid.entity.Bid;
import org.chzz.market.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BidRepositoryCustom {
    Page<BiddingRecord> findUsersBidHistory(User user, Pageable pageable);

    Optional<Bid> findWinningBid(Auction auction);
}
