package org.chzz.market.domain.like.service;

import static org.chzz.market.domain.auction.error.AuctionErrorCode.AUCTION_NOT_FOUND;

import lombok.RequiredArgsConstructor;
import org.chzz.market.common.aop.redisrock.DistributedLock;
import org.chzz.market.domain.auction.error.AuctionException;
import org.chzz.market.domain.auction.repository.AuctionRepository;
import org.chzz.market.domain.like.entity.Like;
import org.chzz.market.domain.like.repository.LikeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LikeUpdateService {
    private final AuctionRepository auctionRepository;
    private final LikeRepository likeRepository;

    @DistributedLock(key = "'like:' + #userId + ':' + #auctionId")
    public void updateLike(Long userId, Long auctionId) {
        // 락 획득 후 트랜잭션 시작
        handleLikeTransaction(userId, auctionId);
    }

    @Transactional
    public void handleLikeTransaction(Long userId, Long auctionId) {
        auctionRepository.findById(auctionId)
                .orElseThrow(() -> new AuctionException(AUCTION_NOT_FOUND));

        likeRepository.findByUserIdAndAuctionId(userId, auctionId)
                .ifPresentOrElse(
                        like -> handleUnlike(like, auctionId),
                        () -> handleLike(userId, auctionId)
                );
    }

    private void handleUnlike(Like like, Long auctionId) {
        likeRepository.delete(like);
        auctionRepository.decrementLikeCount(auctionId);
    }

    private void handleLike(Long userId, Long auctionId) {
        likeRepository.save(createLike(userId, auctionId));
        auctionRepository.incrementLikeCount(auctionId);
    }

    private Like createLike(Long userId, Long auctionId) {
        return Like.builder()
                .userId(userId)
                .auctionId(auctionId)
                .build();
    }
}
