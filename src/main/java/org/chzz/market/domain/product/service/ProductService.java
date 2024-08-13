package org.chzz.market.domain.product.service;

import lombok.RequiredArgsConstructor;
import org.chzz.market.domain.auction.entity.Auction;
import org.chzz.market.domain.auction.repository.AuctionRepository;
import org.chzz.market.domain.image.entity.Image;
import org.chzz.market.domain.image.service.ImageService;
import org.chzz.market.domain.product.dto.DeleteProductResponse;
import org.chzz.market.domain.product.entity.Product;
import org.chzz.market.domain.product.error.ProductErrorCode;
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
import java.util.Optional;

import static org.chzz.market.domain.product.error.ProductErrorCode.*;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ProductService {

    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);

    private final ProductRepository productRepository;
    private final AuctionRepository auctionRepository;
    private final ImageService imageService;

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

        // 경매 유효성 검사
        Optional<Auction> auction = auctionRepository.findByProductId(productId);

        // 경매가 존재하면 경매 상품 삭제, 경매가 존재하지 않으면 사전 등록 상품 삭제
        return auction.map(value -> deleteAuctionProduct(product, value)).orElseGet(() -> deletePreRegisteredProduct(product));
    }

    /*
     * 경매 등록 상품 삭제
     */
    private DeleteProductResponse deleteAuctionProduct(Product product, Auction auction) {
        // 경매 참여자 ID 추출
        List<Long> participantIds = auction.getBids().stream()
                .map(bid -> bid.getBidder().getId())
                .distinct()
                .toList();

        deleteProductImages(product);
        auctionRepository.delete(auction);
        productRepository.delete(product);

        // TODO: 알림 발송 로직 추가
        // NotificationMessage notificationMessage = NotificationMessage.builder()
        //          .userIds(participantIds)
        //          .type(NotificationType.AUCTION_DELETED)
        //          .message("참여하신 경매 상품이 취소되었습니다. 상품명 : " + product.getName()))
        //          .build();
        // notificationService.sendNotification(notificationMessage);

        logger.info("상품 ID {}번, 경매 ID {}번에 해당하는 경매 상품 삭제가 성공적으로 진행되었습니다 (전체 참가자 수: {})", product.getId(), auction.getId(), participantIds.size());

        return DeleteProductResponse.ofAuctioned(product, auction, participantIds.size());
    }

    /*
     * 사전 등록 상품 삭제
     */
    private DeleteProductResponse deletePreRegisteredProduct(Product product) {
        deleteProductImages(product);
        productRepository.delete(product);
        logger.info("상품 ID {}번에 해당하는 사전 등록 상품 삭제가 성공적으로 진행되었습니다.", product.getId());
        return DeleteProductResponse.ofPreRegistered(product);
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
