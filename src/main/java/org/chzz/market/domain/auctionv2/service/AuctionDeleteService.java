package org.chzz.market.domain.auctionv2.service;

import static org.chzz.market.domain.notification.entity.NotificationType.PRE_AUCTION_CANCELED;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.chzz.market.domain.auctionv2.entity.AuctionV2;
import org.chzz.market.domain.auctionv2.error.AuctionErrorCode;
import org.chzz.market.domain.auctionv2.error.AuctionException;
import org.chzz.market.domain.auctionv2.repository.AuctionV2Repository;
import org.chzz.market.domain.imagev2.service.ImageDeleteService;
import org.chzz.market.domain.likev2.repository.LikeV2Repository;
import org.chzz.market.domain.notification.event.NotificationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuctionDeleteService {
    private final ImageDeleteService imageDeleteService;
    private final AuctionV2Repository auctionRepository;
    private final LikeV2Repository likeRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 경매 삭제 (사전등록만 가능)
     */
    @Transactional
    public void delete(Long userId, Long auctionId) {
        AuctionV2 auction = auctionRepository.findById(auctionId) // TODO: 패치 조인으로 쿼리 성능 개선 필요(Seller, Image) n+1 문제
                .orElseThrow(() -> new AuctionException(AuctionErrorCode.AUCTION_NOT_FOUND));
        validate(userId, auction);
        imageDeleteService.deleteImages(auction.getImages());
        auctionRepository.delete(auction);
        processDeleteNotification(auction);
    }

    /**
     * 경매 취소 유효성 검사
     */
    private static void validate(Long userId, AuctionV2 auction) {
        if (auction.isNowOwner(userId)) {
            throw new AuctionException(AuctionErrorCode.AUCTION_ACCESS_FORBIDDEN);
        }
        if (auction.isOfficialAuction()) {
            throw new AuctionException(AuctionErrorCode.OFFICIAL_AUCTION_DELETE_FORBIDDEN);
        }
    }

    /**
     * 사전 경매 취소 알림 이벤트 발행
     */
    private void processDeleteNotification(AuctionV2 auction) {
        // 1. 해당 경매에 좋아요 누른 사용자 ID 추출
        List<Long> likedUserIds = likeRepository.findByAuctionId(auction.getId()).stream().map(like -> like.getUserId())
                .toList();

        // 2. 사젼 경매 취소 알림 이벤트 발행
        if (!likedUserIds.isEmpty()) {
            eventPublisher.publishEvent(NotificationEvent.createSimpleNotification(likedUserIds, PRE_AUCTION_CANCELED,
                    PRE_AUCTION_CANCELED.getMessage(auction.getName()), null));
        }
    }
}
