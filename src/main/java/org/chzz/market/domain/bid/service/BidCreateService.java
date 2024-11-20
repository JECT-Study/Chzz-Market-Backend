package org.chzz.market.domain.bid.service;

import static org.chzz.market.domain.auctionv2.error.AuctionErrorCode.AUCTION_NOT_FOUND;
import static org.chzz.market.domain.bid.error.BidErrorCode.BID_BELOW_MIN_PRICE;
import static org.chzz.market.domain.bid.error.BidErrorCode.BID_BY_OWNER;
import static org.chzz.market.domain.user.error.UserErrorCode.USER_NOT_FOUND;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.chzz.market.common.aop.redisrock.DistributedLock;
import org.chzz.market.domain.auctionv2.entity.AuctionV2;
import org.chzz.market.domain.auctionv2.error.AuctionException;
import org.chzz.market.domain.auctionv2.repository.AuctionV2Repository;
import org.chzz.market.domain.bid.dto.BidCreateRequest;
import org.chzz.market.domain.bid.error.BidException;
import org.chzz.market.domain.bid.repository.BidRepository;
import org.chzz.market.domain.user.entity.User;
import org.chzz.market.domain.user.error.exception.UserException;
import org.chzz.market.domain.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class BidCreateService {
    private final AuctionV2Repository auctionRepository;
    private final BidRepository bidRepository;
    private final UserRepository userRepository;

    @Transactional
    @DistributedLock(key = "'bid:' + #userId + ':' + #bidCreateRequest.auctionId")
    public void create(final BidCreateRequest bidCreateRequest, Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserException(USER_NOT_FOUND));
        AuctionV2 auction = auctionRepository.findById(bidCreateRequest.getAuctionId())
                .orElseThrow(() -> new AuctionException(AUCTION_NOT_FOUND));
        validateBidConditions(bidCreateRequest, user.getId(), auction);
        bidRepository.findByAuctionIdAndBidderId(auction.getId(), userId)
                .ifPresentOrElse(
                        // 이미 입찰을 한 경우
                        bid -> bid.adjustBidAmount(bidCreateRequest.getBidAmount()),
                        // 입찰을 처음 하는 경우
                        () -> {
                            bidRepository.save(bidCreateRequest.toEntity(user.getId()));
                            auctionRepository.incrementBidCount(auction.getId());
                        }
                );
    }

    /**
     * 입찰 상태 유효성 검사
     */
    private void validateBidConditions(BidCreateRequest bidCreateRequest, Long userId, AuctionV2 auction) {
        // 경매 등록자가 입찰할 때
        if (auction.isOwner(userId)) {
            throw new BidException(BID_BY_OWNER);
        }
        auction.validateAuctionEndTime();
        // 최소 금액보다 낮은 금액일 때
        if (!auction.isAboveMinPrice(bidCreateRequest.getBidAmount())) {
            throw new BidException(BID_BELOW_MIN_PRICE);
        }
    }
}
