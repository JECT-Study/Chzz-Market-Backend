package org.chzz.market.domain.auction.service;

import static org.chzz.market.common.error.GlobalErrorCode.RESOURCE_NOT_FOUND;
import static org.chzz.market.domain.auction.error.AuctionErrorCode.AUCTION_NOT_FOUND;
import static org.chzz.market.domain.auction.error.AuctionErrorCode.NOT_WINNER;

import lombok.RequiredArgsConstructor;
import org.chzz.market.common.error.GlobalException;
import org.chzz.market.domain.auction.dto.response.WonAuctionDetailsResponse;
import org.chzz.market.domain.auction.entity.Auction;
import org.chzz.market.domain.auction.error.AuctionException;
import org.chzz.market.domain.auction.repository.AuctionQueryRepository;
import org.chzz.market.domain.auction.repository.AuctionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuctionWonService {
    private final AuctionRepository auctionRepository;
    private final AuctionQueryRepository auctionQueryRepository;

    /**
     * 낙찰 정보 조회
     */
    public WonAuctionDetailsResponse getWinningBidByAuctionId(Long userId, Long auctionId) {
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new AuctionException(AUCTION_NOT_FOUND));
        if (!auction.isWinner(userId)) {
            throw new AuctionException(NOT_WINNER);
        }
        return auctionQueryRepository.findWinningBidById(auctionId)
                .orElseThrow(() -> new GlobalException(RESOURCE_NOT_FOUND));
    }
}
