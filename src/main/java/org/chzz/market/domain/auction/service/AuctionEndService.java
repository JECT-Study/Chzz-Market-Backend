package org.chzz.market.domain.auction.service;

import static org.chzz.market.domain.notification.entity.NotificationType.AUCTION_FAILURE;
import static org.chzz.market.domain.notification.entity.NotificationType.AUCTION_NON_WINNER;
import static org.chzz.market.domain.notification.entity.NotificationType.AUCTION_SUCCESS;
import static org.chzz.market.domain.notification.entity.NotificationType.AUCTION_WINNER;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.chzz.market.domain.auction.dto.event.AuctionEndEvent;
import org.chzz.market.domain.auction.entity.Auction;
import org.chzz.market.domain.auction.error.AuctionErrorCode;
import org.chzz.market.domain.auction.error.AuctionException;
import org.chzz.market.domain.auction.repository.AuctionRepository;
import org.chzz.market.domain.bid.entity.Bid;
import org.chzz.market.domain.bid.repository.BidQueryRepository;
import org.chzz.market.domain.notification.event.NotificationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuctionEndService {
    private final AuctionRepository auctionRepository;
    private final BidQueryRepository bidRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void endAuction(final Long auctionId) {
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new AuctionException(AuctionErrorCode.AUCTION_NOT_FOUND));

        auction.endAuction();
        notifyAuctionEnded(auction);
        eventPublisher.publishEvent(new AuctionEndEvent(auction));
    }

    /**
     * 판매자에게 경매 종료 알림
     */
    private void notifyAuctionEnded(Auction auction) {
        Long sellerId = auction.getSeller().getId();
        String auctionName = auction.getName();
        String firstImageCdnPath = auction.getFirstImageCdnPath();
        List<Bid> bids = bidRepository.findAllBidsByAuction(auction);

        if (bids.isEmpty()) { // 입찰이 없는 경우
            eventPublisher.publishEvent(
                    NotificationEvent.createSimpleNotification(sellerId, AUCTION_FAILURE,
                            AUCTION_FAILURE.getMessage(auctionName),
                            firstImageCdnPath)); // 낙찰 실패 알림 이벤트
            return;
        }
        eventPublisher.publishEvent(
                NotificationEvent.createAuctionNotification(sellerId, AUCTION_SUCCESS,
                        AUCTION_SUCCESS.getMessage(auctionName),
                        firstImageCdnPath, auction.getId())); // 낙찰 성공 알림 이벤트

        alterWinner(auction, bids.get(0), auctionName, firstImageCdnPath); // 첫 번째 입찰이 낙찰
        notifyNonWinner(bids, auctionName, firstImageCdnPath);

    }

    /**
     * 낙찰자에게 알림 전송
     */
    private void alterWinner(Auction auction, Bid winningBid, String auctionName, String firstImageCdnPath) {
        auction.assignWinner(winningBid.getBidderId());
        eventPublisher.publishEvent(
                NotificationEvent.createAuctionNotification(winningBid.getBidderId(), AUCTION_WINNER,
                        AUCTION_WINNER.getMessage(auctionName), firstImageCdnPath, auction.getId())); // 낙찰자 알림 이벤트
        log.info("경매 ID {}: 낙찰자 처리 완료", auction.getId());
    }

    /**
     * 미낙찰자들에게 알림 전송
     */
    private void notifyNonWinner(List<Bid> bids, String auctionName, String firstImageCdnPath) {
        List<Long> nonWinnerIds = bids.stream().skip(1) // 낙찰자를 제외한 나머지 입찰자들
                .map(Bid::getBidderId).collect(Collectors.toList());

        if (!nonWinnerIds.isEmpty()) {
            eventPublisher.publishEvent(NotificationEvent.createSimpleNotification(nonWinnerIds, AUCTION_NON_WINNER,
                    AUCTION_NON_WINNER.getMessage(auctionName), firstImageCdnPath)); // 미낙찰자 알림 이벤트
        }
    }
}
