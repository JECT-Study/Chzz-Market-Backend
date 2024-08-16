package org.chzz.market.domain.like.service;

import lombok.RequiredArgsConstructor;
import org.chzz.market.domain.like.dto.LikeResponse;
import org.chzz.market.domain.like.entity.Like;
import org.chzz.market.domain.like.error.LikeException;
import org.chzz.market.domain.like.repository.LikeRepository;
import org.chzz.market.domain.product.entity.Product;
import org.chzz.market.domain.product.error.ProductException;
import org.chzz.market.domain.product.repository.ProductRepository;
import org.chzz.market.domain.user.entity.User;
import org.chzz.market.domain.user.error.exception.UserException;
import org.chzz.market.domain.user.repository.UserRepository;
import org.hibernate.dialect.lock.OptimisticEntityLockException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static org.chzz.market.domain.like.error.LikeErrorCode.LIKE_NOT_FOUND;
import static org.chzz.market.domain.product.error.ProductErrorCode.*;
import static org.chzz.market.domain.user.error.UserErrorCode.USER_NOT_FOUND;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class LikeService {

    private static final Logger logger = LoggerFactory.getLogger(LikeService.class);

    private final ProductRepository productRepository;
    private final LikeRepository likeRepository;
    private final UserRepository userRepository;

    @Transactional
    @Retryable(
            retryFor = {OptimisticEntityLockException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000)
    )
    public LikeResponse toggleLike(Long userId, Long productId) {
        logger.info("상품 ID {}번 상품에 유저 ID {}번의 유저가 좋아요 토글 API를 호출합니다.", productId, userId);
        Product product = findProductForLike(productId);
        User user = findUser(userId);

        boolean isLiked = checkLikeExists(userId, productId);

        LikeResponse response;
        if (isLiked) {
            response = unlikeProduct(user, product);
            logger.info("유저 ID {}번의 유저가 상품 ID {}번의 상품 좋아요를 취소했습니다.", userId, productId);
        } else {
            response = likeProduct(user, product);
            logger.info("유저 ID {}번의 유저가 상품 ID {}번의 상품 좋아요를 눌렀습니다.", userId, productId);
        }

        logger.info("좋아요 토글 기능이 완료되었습니다. 좋아요: {}, 좋아요 개수: {}", response.isLiked(), product.getLikeCount());
        return response;
    }

    @Transactional
    public LikeResponse unlikeProduct(User user, Product product) {
        Like like = likeRepository.findByUserAndProduct(user, product)
                .orElseThrow(() -> new LikeException(LIKE_NOT_FOUND));
        product.removeLike(like);
        likeRepository.delete(like);
        return LikeResponse.of(false, product.getLikeCount());
    }

    @Transactional
    public LikeResponse likeProduct(User user, Product product) {
        Like newLike = Like.builder()
                .user(user)
                .product(product)
                .build();
        product.addLike(newLike);
        likeRepository.save(newLike);
        return LikeResponse.of(true, product.getLikeCount());
    }

    public Product findProductForLike(Long productId) {
        logger.debug("상품 ID {}번의 상품 좋아요를 위한 상품 조회를 시작합니다.", productId);
        return productRepository.findProductForLike(productId)
                .orElseThrow(() -> new ProductException(PRODUCT_NOT_FOUND_OR_IN_AUCTION));
    }

    public User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserException(USER_NOT_FOUND));
    }

    public boolean checkLikeExists(Long userId, Long productId) {
        return likeRepository.existsByUserIdAndProductId(userId, productId);
    }
}
