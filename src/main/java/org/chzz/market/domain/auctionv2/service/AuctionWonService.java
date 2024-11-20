package org.chzz.market.domain.auctionv2.service;

import static org.chzz.market.common.error.GlobalErrorCode.RESOURCE_NOT_FOUND;
import static org.chzz.market.domain.auctionv2.error.AuctionErrorCode.AUCTION_NOT_FOUND;
import static org.chzz.market.domain.auctionv2.error.AuctionErrorCode.NOW_WINNER;

import lombok.RequiredArgsConstructor;
import org.chzz.market.common.error.GlobalException;
import org.chzz.market.domain.auctionv2.dto.response.WonAuctionDetailsResponse;
import org.chzz.market.domain.auctionv2.entity.AuctionV2;
import org.chzz.market.domain.auctionv2.error.AuctionException;
import org.chzz.market.domain.auctionv2.repository.AuctionV2QueryRepository;
import org.chzz.market.domain.auctionv2.repository.AuctionV2Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuctionWonService {
    private final AuctionV2Repository auctionRepository;
    private final AuctionV2QueryRepository auctionQueryRepository;

    /**
     * 낙찰 정보 조회
     */
    public WonAuctionDetailsResponse getWinningBidByAuctionId(Long userId, Long auctionId) {
        AuctionV2 auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new AuctionException(AUCTION_NOT_FOUND));
        if (!auction.isWinner(userId)) {
            throw new AuctionException(NOW_WINNER);
        }
        return auctionQueryRepository.findWinningBidById(auctionId)
                .orElseThrow(() -> new GlobalException(RESOURCE_NOT_FOUND));
    }
}
