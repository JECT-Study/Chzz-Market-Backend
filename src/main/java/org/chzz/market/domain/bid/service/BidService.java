package org.chzz.market.domain.bid.service;

import static org.chzz.market.domain.auction.error.AuctionErrorCode.AUCTION_NOT_FOUND;
import static org.chzz.market.domain.auction.error.AuctionErrorCode.FORBIDDEN_AUCTION_ACCESS;
import static org.chzz.market.domain.bid.error.BidErrorCode.BID_BELOW_MIN_PRICE;
import static org.chzz.market.domain.bid.error.BidErrorCode.BID_BY_OWNER;
import static org.chzz.market.domain.bid.error.BidErrorCode.BID_NOT_ACCESSIBLE;
import static org.chzz.market.domain.bid.error.BidErrorCode.BID_NOT_FOUND;
import static org.chzz.market.domain.user.error.UserErrorCode.USER_NOT_FOUND;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.chzz.market.domain.auction.entity.Auction;
import org.chzz.market.domain.auction.error.AuctionException;
import org.chzz.market.domain.auction.repository.AuctionRepository;
import org.chzz.market.domain.bid.dto.BidCreateRequest;
import org.chzz.market.domain.bid.dto.query.BiddingRecord;
import org.chzz.market.domain.bid.dto.response.BidInfoResponse;
import org.chzz.market.domain.bid.entity.Bid;
import org.chzz.market.domain.bid.error.BidException;
import org.chzz.market.domain.bid.repository.BidRepository;
import org.chzz.market.domain.user.entity.User;
import org.chzz.market.domain.user.error.exception.UserException;
import org.chzz.market.domain.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class BidService {
    private final AuctionRepository auctionRepository;
    private final BidRepository bidRepository;
    private final UserRepository userRepository;


    public Page<BiddingRecord> inquireBidHistory(Long userId, Pageable pageable) {
        return bidRepository.findUsersBidHistory(userId, pageable);
    }

    @Transactional
    public void createBid(final BidCreateRequest bidCreateRequest, Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserException(USER_NOT_FOUND));
        Auction auction = auctionRepository.findById(bidCreateRequest.getAuctionId())
                .orElseThrow(() -> new AuctionException(AUCTION_NOT_FOUND));
        validateBidConditions(bidCreateRequest, user.getId(), auction);
        bidRepository.findByAuctionAndBidder(auction, user)
                .ifPresentOrElse(
                        // 이미 입찰을 한 경우
                        bid -> bid.adjustBidAmount(bidCreateRequest.getAmount()),
                        // 입찰을 처음 하는 경우
                        () -> auction.registerBid(bidCreateRequest.toEntity(auction, user)) // 연관관계 설정
                );
    }

    @Transactional
    public void cancelBid(Long bidId, Long userId) {
        Bid bid = bidRepository.findById(bidId).orElseThrow(() -> new BidException(BID_NOT_FOUND));
        Auction auction = bid.getAuction();
        if (!bid.isOwner(userId)) {
            throw new BidException(BID_NOT_ACCESSIBLE);
        }
        auction.validateAuctionEndTime();
        auction.removeBid(bid);
        log.info("입찰이 취소되었습니다. 입찰 ID: {}, 사용자 ID: {}, 경매 ID: {}", bid.getId(), userId, auction.getId());
    }

    public List<Bid> findAllBidsByAuction(Auction auction) {
        return bidRepository.findAllBidsByAuction(auction);
    }

    public Page<BidInfoResponse> getBidsByAuctionId(Long userId, Long auctionId, Pageable pageable) {
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new AuctionException(AUCTION_NOT_FOUND));
        if (!auction.getProduct().isOwner(userId)) {
            throw new AuctionException(FORBIDDEN_AUCTION_ACCESS);
        }
        auction.validateAuctionEnded();
        return bidRepository.findBidsByAuctionId(auctionId, pageable);
    }

    private void validateBidConditions(BidCreateRequest bidCreateRequest, Long userId, Auction auction) {
        // 경매 등록자가 입찰할 때
        if (auction.getProduct().isOwner(userId)) {
            throw new BidException(BID_BY_OWNER);
        }
        auction.validateAuctionEndTime();
        // 최소 금액보다 낮은 금액일 때
        if (!auction.isAboveMinPrice(bidCreateRequest.getAmount())) {
            throw new BidException(BID_BELOW_MIN_PRICE);
        }
    }
}
