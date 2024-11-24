package org.chzz.market.domain.auctionv2.service;

import static org.chzz.market.domain.notification.entity.NotificationType.AUCTION_FAILURE;
import static org.chzz.market.domain.notification.entity.NotificationType.AUCTION_NON_WINNER;
import static org.chzz.market.domain.notification.entity.NotificationType.AUCTION_SUCCESS;
import static org.chzz.market.domain.notification.entity.NotificationType.AUCTION_WINNER;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.chzz.market.domain.auctionv2.entity.AuctionV2;
import org.chzz.market.domain.auctionv2.error.AuctionErrorCode;
import org.chzz.market.domain.auctionv2.error.AuctionException;
import org.chzz.market.domain.auctionv2.repository.AuctionV2Repository;
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
    private final AuctionV2Repository auctionV2Repository;
    private final BidQueryRepository bidRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void endAuction(final Long auctionId) {
        AuctionV2 auction = auctionV2Repository.findById(auctionId)
                .orElseThrow(() -> new AuctionException(AuctionErrorCode.AUCTION_NOT_FOUND));

        auction.endAuction();
        notifyAuctionEnded(auction);
    }

    /**
     * 판매자에게 경매 종료 알림
     */
    private void notifyAuctionEnded(AuctionV2 auction) {
        Long sellerId = auction.getSeller().getId();
        String productName = auction.getName();
        String firstImageCdnPath = auction.getFirstImageCdnPath();
        List<Bid> bids = bidRepository.findAllBidsByAuction(auction);

        if (bids.isEmpty()) { // 입찰이 없는 경우
            eventPublisher.publishEvent(
                    NotificationEvent.createSimpleNotification(sellerId, AUCTION_FAILURE,
                            AUCTION_FAILURE.getMessage(productName),
                            firstImageCdnPath)); // 낙찰 실패 알림 이벤트
            return;
        }
        eventPublisher.publishEvent(
                NotificationEvent.createAuctionNotification(sellerId, AUCTION_SUCCESS,
                        AUCTION_SUCCESS.getMessage(productName),
                        firstImageCdnPath, auction.getId())); // 낙찰 성공 알림 이벤트

        alter2Winner(auction, bids.get(0), productName, firstImageCdnPath); // 첫 번째 입찰이 낙찰
        notify2NonWinner(bids, productName, firstImageCdnPath);

    }

    /**
     * 낙찰자에게 알림 전송
     */
    private void alter2Winner(AuctionV2 auction, Bid winningBid, String productName, String firstImageCdnPath) {
        auction.assignWinner(winningBid.getBidderId());
        eventPublisher.publishEvent(
                NotificationEvent.createAuctionNotification(winningBid.getBidderId(), AUCTION_WINNER,
                        AUCTION_WINNER.getMessage(productName), firstImageCdnPath, auction.getId())); // 낙찰자 알림 이벤트
        log.info("경매 ID {}: 낙찰자 처리 완료", auction.getId());
    }

    /**
     * 미낙찰자들에게 알림 전송
     */
    private void notify2NonWinner(List<Bid> bids, String productName, String firstImageCdnPath) {
        List<Long> nonWinnerIds = bids.stream().skip(1) // 낙찰자를 제외한 나머지 입찰자들
                .map(Bid::getBidderId).collect(Collectors.toList());

        if (!nonWinnerIds.isEmpty()) {
            eventPublisher.publishEvent(NotificationEvent.createSimpleNotification(nonWinnerIds, AUCTION_NON_WINNER,
                    AUCTION_NON_WINNER.getMessage(productName), firstImageCdnPath)); // 미낙찰자 알림 이벤트
        }
    }
}
