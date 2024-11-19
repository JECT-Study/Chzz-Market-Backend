package org.chzz.market.domain.likev2.service;

import static org.chzz.market.domain.auctionv2.error.AuctionErrorCode.AUCTION_NOT_FOUND;

import lombok.RequiredArgsConstructor;
import org.chzz.market.domain.auctionv2.error.AuctionException;
import org.chzz.market.domain.auctionv2.repository.AuctionV2Repository;
import org.chzz.market.domain.likev2.entity.LikeV2;
import org.chzz.market.domain.likev2.repository.LikeV2Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LikeUpdateService {
    private final AuctionV2Repository auctionRepository;
    private final LikeV2Repository likeRepository;

    @Transactional
    public void updateLike(Long userId, Long auctionId) {
        auctionRepository.findById(auctionId)
                .orElseThrow(() -> new AuctionException(AUCTION_NOT_FOUND));

        likeRepository.findByUserIdAndAuctionId(userId, auctionId)
                .ifPresentOrElse(
                        like -> handleUnlike(like, auctionId),
                        () -> handleLike(userId, auctionId)
                );
    }

    private void handleUnlike(LikeV2 like, Long auctionId) {
        likeRepository.delete(like);
        auctionRepository.decrementLikeCount(auctionId); // TODO: 동시성문제 고려 일단 원자적 연산으로 해결
    }

    private void handleLike(Long userId, Long auctionId) {
        likeRepository.save(createLike(userId, auctionId));
        auctionRepository.incrementLikeCount(auctionId); // TODO: 동시성문제 고려 일단 원자적 연산으로 해결
    }

    private LikeV2 createLike(Long userId, Long auctionId) {
        return LikeV2.builder()
                .userId(userId)
                .auctionId(auctionId)
                .build();
    }
}
