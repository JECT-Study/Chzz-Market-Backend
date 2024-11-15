package org.chzz.market.domain.bid.repository;

import java.util.Optional;
import org.chzz.market.domain.bid.entity.Bid;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BidRepository extends JpaRepository<Bid, Long>, BidRepositoryCustom {
    Optional<Bid> findByAuctionIdAndBidderId(Long auctionId, Long userId);
}
