package org.chzz.market.domain.bid.service;

import static org.chzz.market.domain.auctionv2.error.AuctionErrorCode.AUCTION_ACCESS_FORBIDDEN;
import static org.chzz.market.domain.auctionv2.error.AuctionErrorCode.AUCTION_NOT_FOUND;

import lombok.RequiredArgsConstructor;
import org.chzz.market.domain.auctionv2.entity.AuctionV2;
import org.chzz.market.domain.auctionv2.error.AuctionException;
import org.chzz.market.domain.auctionv2.repository.AuctionV2Repository;
import org.chzz.market.domain.bid.dto.response.BidInfoResponse;
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

    public Page<BidInfoResponse> getBidsByAuctionId(Long userId, Long auctionId, Pageable pageable) {
        AuctionV2 auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new AuctionException(AUCTION_NOT_FOUND));
        if (!auction.isOwner(userId)) {
            throw new AuctionException(AUCTION_ACCESS_FORBIDDEN);
        }
        auction.validateAuctionEnded();
        return bidQueryRepository.findBidsByAuctionId(auctionId, pageable);
    }
}
