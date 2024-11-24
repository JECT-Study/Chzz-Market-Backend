package org.chzz.market.domain.auctionv2.service;

import static org.chzz.market.domain.auctionv2.error.AuctionErrorCode.AUCTION_NOT_FOUND;
import static org.chzz.market.domain.notification.entity.NotificationType.AUCTION_START;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.chzz.market.domain.auctionv2.dto.AuctionRegistrationEvent;
import org.chzz.market.domain.auctionv2.entity.AuctionV2;
import org.chzz.market.domain.auctionv2.error.AuctionException;
import org.chzz.market.domain.auctionv2.repository.AuctionV2Repository;
import org.chzz.market.domain.likev2.repository.LikeV2Repository;
import org.chzz.market.domain.notification.event.NotificationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class AuctionStartService {
    private final AuctionV2Repository auctionV2Repository;
    private final LikeV2Repository likeRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 사전 등록 상품 경매 전환 처리
     */
    @Transactional
    public void start(Long userId, Long auctionId) {
        AuctionV2 auction = auctionV2Repository.findById(auctionId)
                .orElseThrow(() -> new AuctionException(AUCTION_NOT_FOUND));
        auction.validateOwner(userId);
        auction.startOfficialAuction();
        eventPublisher.publishEvent(new AuctionRegistrationEvent(auction.getId(), auction.getEndDateTime()));
        processStartNotification(auction);
        log.info("{}번 경매 정식경매 전환완료", auctionId);
    }

    private void processStartNotification(AuctionV2 auction) {
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
