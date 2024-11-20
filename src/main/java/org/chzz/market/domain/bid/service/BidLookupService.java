package org.chzz.market.domain.bid.service;

import static org.chzz.market.domain.auctionv2.error.AuctionErrorCode.AUCTION_ACCESS_FORBIDDEN;
import static org.chzz.market.domain.auctionv2.error.AuctionErrorCode.AUCTION_NOT_FOUND;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.chzz.market.domain.auctionv2.entity.AuctionStatus;
import org.chzz.market.domain.auctionv2.entity.AuctionV2;
import org.chzz.market.domain.auctionv2.error.AuctionException;
import org.chzz.market.domain.auctionv2.repository.AuctionV2Repository;
import org.chzz.market.domain.bid.dto.query.BiddingRecord;
import org.chzz.market.domain.bid.dto.response.BidInfoResponse;
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
    private final AuctionV2Repository auctionRepository;
    private final BidQueryRepository bidQueryRepository;

    /**
     * 특정 경매의 모든 입찰 조회
     */
    public Page<BidInfoResponse> getBidsByAuctionId(Long userId, Long auctionId, Pageable pageable) {
        AuctionV2 auction = auctionRepository.findById(auctionId)
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
    public List<Bid> findAllBidsByAuction(AuctionV2 auction) {
        return bidQueryRepository.findAllBidsByAuction(auction);
    }
}
