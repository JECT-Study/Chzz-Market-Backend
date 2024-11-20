package org.chzz.market.domain.bid.service;

import static org.chzz.market.domain.bid.error.BidErrorCode.BID_NOT_ACCESSIBLE;
import static org.chzz.market.domain.bid.error.BidErrorCode.BID_NOT_FOUND;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.chzz.market.domain.bid.entity.Bid;
import org.chzz.market.domain.bid.error.BidException;
import org.chzz.market.domain.bid.repository.BidRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class BidCancelService {
    private final BidRepository bidRepository;
    private final BidCancelLockService bidCancelLockService;

    /**
     * 입찰 취소
     */
    public void cancel(Long bidId, Long userId) {
        Bid bid = bidRepository.findById(bidId).orElseThrow(() -> new BidException(BID_NOT_FOUND));
        if (!bid.isOwner(userId)) {
            throw new BidException(BID_NOT_ACCESSIBLE);
        }
        bidCancelLockService.cancel(bid.getAuctionId(), bidId, userId);
    }
}
