package org.chzz.market.domain.auction.service;

import static org.chzz.market.domain.notification.entity.NotificationType.PRE_AUCTION_CANCELED;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.chzz.market.domain.auction.entity.Auction;
import org.chzz.market.domain.auction.error.AuctionErrorCode;
import org.chzz.market.domain.auction.error.AuctionException;
import org.chzz.market.domain.auction.repository.AuctionRepository;
import org.chzz.market.domain.image.service.ImageDeleteService;
import org.chzz.market.domain.like.repository.LikeRepository;
import org.chzz.market.domain.notification.event.NotificationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuctionDeleteService {
    private final ImageDeleteService imageDeleteService;
    private final AuctionRepository auctionRepository;
    private final LikeRepository likeRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 경매 삭제 (사전등록만 가능)
     */
    @Transactional
    public void delete(Long userId, Long auctionId) {
        Auction auction = auctionRepository.findById(auctionId) // TODO: 패치 조인으로 쿼리 성능 개선 필요(Seller, Image) n+1 문제
                .orElseThrow(() -> new AuctionException(AuctionErrorCode.AUCTION_NOT_FOUND));
        validate(userId, auction);
        imageDeleteService.deleteImages(auction.getImages());
        auctionRepository.delete(auction);
        processDeleteNotification(auction);
    }

    /**
     * 경매 취소 유효성 검사
     */
    private static void validate(Long userId, Auction auction) {
        auction.validateOwner(userId);
        if (auction.isOfficialAuction()) {
            throw new AuctionException(AuctionErrorCode.OFFICIAL_AUCTION_DELETE_FORBIDDEN);
        }
    }

    /**
     * 사전 경매 취소 알림 이벤트 발행
     */
    private void processDeleteNotification(Auction auction) {
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
