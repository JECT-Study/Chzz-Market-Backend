package org.chzz.market.domain.product.service;

import lombok.RequiredArgsConstructor;
import org.chzz.market.domain.auction.repository.AuctionRepository;
import org.chzz.market.domain.image.entity.Image;
import org.chzz.market.domain.image.service.ImageService;
import org.chzz.market.domain.product.dto.DeleteProductResponse;
import org.chzz.market.domain.product.entity.Product;
import org.chzz.market.domain.product.error.ProductException;
import org.chzz.market.domain.product.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.chzz.market.domain.product.error.ProductErrorCode.*;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ProductService {

    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);

    private final ProductRepository productRepository;
    private final AuctionRepository auctionRepository;
    private final ImageService imageService;

    /*
     * 사전 등록 상품 삭제
     */
    @Retryable(
            retryFor = {TransientDataAccessException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000)
    )
    @Transactional
    public DeleteProductResponse deleteProduct(Long productId, Long userId) {
        logger.info("상품 ID {}번에 해당하는 상품 삭제 프로세스를 시작합니다.", productId);

        // 상품 유효성 검사
        Product product = productRepository.findByIdAndUserId(productId, userId)
                .orElseThrow(() -> {
                    logger.info("상품 ID {}번에 해당하는 상품을 찾을 수 없습니다.", productId);
                    return new ProductException(PRODUCT_NOT_FOUND);
                });

        // 경매 등록 여부 확인
        if (auctionRepository.existsByProductId(productId)) {
            logger.info("상품 ID {}번은 이미 경매로 등록되어 삭제할 수 없습니다.", productId);
            throw new ProductException(PRODUCT_ALREADY_AUCTIONED);
        }

        // 좋아요 누른 사용자 ID 추출
        List<Long> likedUserIds = product.getLikes().stream()
                .map(like -> like.getUser().getId())
                .distinct()
                .toList();

        deleteProductImages(product);
        productRepository.delete(product);

        // 좋아요 누른 사용자들에게 알림 전송
        // Notification notificationMessage = new Notification(
        //        likedUserIds,
        //        Notificationtype.PRE_REGISTER_DELETED,
        //        product.getName()
        // );
        // notificationService.sendNotification(notificationMessage);

        logger.info("사전 등록 상품 ID{}번에 해당하는 상품을 성공적으로 삭제하였습니다. (좋아요 누른 사용자 수: {})", productId, likedUserIds.size());

        return DeleteProductResponse.ofPreRegistered(product, likedUserIds.size());
    }

    /*
     * 상품 이미지 삭제
     */
    private void deleteProductImages(Product product) {
        List<String> imageUrls = product.getImages().stream()
                .map(Image::getCdnPath)
                .toList();

        imageService.deleteUploadImages(imageUrls);
        logger.info("상품 ID {}번에 해당하는 상품의 이미지를 모두 삭제하였습니다.", product.getId());
    }

}
