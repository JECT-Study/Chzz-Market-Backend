package org.chzz.market.domain.bid.repository;

import java.util.List;
import org.chzz.market.domain.auction.entity.Auction;
import org.chzz.market.domain.auction.type.AuctionStatus;
import org.chzz.market.domain.bid.dto.query.BiddingRecord;
import org.chzz.market.domain.bid.dto.response.BidInfoResponse;
import org.chzz.market.domain.bid.entity.Bid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BidRepositoryCustom {
    Page<BiddingRecord> findUsersBidHistory(Long userId, Pageable pageable, AuctionStatus status);

    List<Bid> findAllBidsByAuction(Auction auction);

    Page<BidInfoResponse> findBidsByAuctionId(Long auctionId, Pageable pageable);
}
