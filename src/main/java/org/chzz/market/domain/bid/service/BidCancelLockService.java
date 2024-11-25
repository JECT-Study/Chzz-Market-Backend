package org.chzz.market.domain.bid.service;

import static org.chzz.market.domain.bid.error.BidErrorCode.BID_NOT_FOUND;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.chzz.market.common.aop.redisrock.DistributedLock;
import org.chzz.market.domain.auction.repository.AuctionRepository;
import org.chzz.market.domain.bid.entity.Bid;
import org.chzz.market.domain.bid.error.BidException;
import org.chzz.market.domain.bid.repository.BidRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class BidCancelLockService {

    private final AuctionRepository auctionRepository;
    private final BidRepository bidRepository;

    /**
     * 분산 락을 사용한 입찰 취소
     */
    @Transactional
    @DistributedLock(key = "'bid:' + #userId + ':' + #auctionId")
    public void cancel(Long auctionId, Long bidId, Long userId) {
        Bid bid = bidRepository.findById(bidId).orElseThrow(() -> new BidException(BID_NOT_FOUND));
        bid.cancelBid();
        auctionRepository.decrementBidCount(auctionId);
        log.info("입찰이 취소되었습니다. 입찰 ID: {}, 사용자 ID: {}, 경매 ID: {}", bid.getId(), userId, auctionId);
    }
}
