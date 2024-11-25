package org.chzz.market.domain.auction.service;

import static org.chzz.market.domain.auction.error.AuctionErrorCode.AUCTION_NOT_FOUND;
import static org.chzz.market.domain.notification.entity.NotificationType.AUCTION_START;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.chzz.market.domain.auction.dto.AuctionRegistrationEvent;
import org.chzz.market.domain.auction.entity.Auction;
import org.chzz.market.domain.auction.error.AuctionException;
import org.chzz.market.domain.auction.repository.AuctionRepository;
import org.chzz.market.domain.like.repository.LikeRepository;
import org.chzz.market.domain.notification.event.NotificationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class AuctionStartService {
    private final AuctionRepository auctionRepository;
    private final LikeRepository likeRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 사전 등록 상품 경매 전환 처리
     */
    @Transactional
    public void start(Long userId, Long auctionId) {
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new AuctionException(AUCTION_NOT_FOUND));
        auction.validateOwner(userId);
        auction.startOfficialAuction();
        eventPublisher.publishEvent(new AuctionRegistrationEvent(auction.getId(), auction.getEndDateTime()));
        processStartNotification(auction);
        log.info("{}번 경매 정식경매 전환완료", auctionId);
    }

    private void processStartNotification(Auction auction) {
        // 1. 해당 경매에 좋아요 누른 사용자 ID 추출
        List<Long> likedUserIds = likeRepository.findByAuctionId(auction.getId()).stream().map(like -> like.getUserId())
                .toList();

        // 2. 경매 시작 알림 이벤트 발행
        if (!likedUserIds.isEmpty()) {
            eventPublisher.publishEvent(NotificationEvent.createAuctionNotification(likedUserIds, AUCTION_START,
                    AUCTION_START.getMessage(auction.getName()),
                    auction.getFirstImageCdnPath(), auction.getId()));
        }
    }
}
