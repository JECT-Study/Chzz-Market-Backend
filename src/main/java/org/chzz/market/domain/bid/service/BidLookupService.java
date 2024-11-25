package org.chzz.market.domain.bid.service;

import static org.chzz.market.domain.auction.error.AuctionErrorCode.AUCTION_ACCESS_FORBIDDEN;
import static org.chzz.market.domain.auction.error.AuctionErrorCode.AUCTION_NOT_FOUND;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.chzz.market.domain.auction.entity.Auction;
import org.chzz.market.domain.auction.entity.AuctionStatus;
import org.chzz.market.domain.auction.error.AuctionException;
import org.chzz.market.domain.auction.repository.AuctionRepository;
import org.chzz.market.domain.bid.dto.response.BidInfoResponse;
import org.chzz.market.domain.bid.dto.response.BiddingRecord;
import org.chzz.market.domain.bid.entity.Bid;
import org.chzz.market.domain.bid.repository.BidQueryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BidLookupService {
    private final AuctionRepository auctionRepository;
    private final BidQueryRepository bidQueryRepository;

    /**
     * 특정 경매의 모든 입찰 조회
     */
    public Page<BidInfoResponse> getBidsByAuctionId(Long userId, Long auctionId, Pageable pageable) {
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new AuctionException(AUCTION_NOT_FOUND));
        if (!auction.isOwner(userId)) {
            throw new AuctionException(AUCTION_ACCESS_FORBIDDEN);
        }
        auction.validateAuctionEnded();
        return bidQueryRepository.findBidsByAuctionId(auctionId, pageable);
    }

    /**
     * 나의 입찰 목록 조회
     */
    public Page<BiddingRecord> inquireBidHistory(Long userId, Pageable pageable, AuctionStatus status) {
        return bidQueryRepository.findUsersBidHistory(userId, pageable, status);
    }

    /**
     * 특정 경매의 입찰 Entity 조회 (경매 종료스케줄링에 사용)
     */
    public List<Bid> findAllBidsByAuction(Auction auction) {
        return bidQueryRepository.findAllBidsByAuction(auction);
    }
}
