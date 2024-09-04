package org.chzz.market.domain.product.service;

import lombok.RequiredArgsConstructor;
import org.chzz.market.domain.auction.repository.AuctionRepository;
import org.chzz.market.domain.image.entity.Image;
import org.chzz.market.domain.image.repository.ImageRepository;
import org.chzz.market.domain.image.service.ImageService;
import org.chzz.market.domain.product.dto.*;
import org.chzz.market.domain.product.entity.Product;
import org.chzz.market.domain.product.error.ProductException;
import org.chzz.market.domain.product.dto.ProductDetailsResponse;
import org.chzz.market.domain.product.dto.ProductResponse;
import org.chzz.market.domain.product.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
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
    private final ImageRepository imageRepository;

    /*
     * 카테고리별 사전 등록 상품 목록 조회
     */
    public Page<ProductResponse> getProductListByCategory(Product.Category category, Long userId, Pageable pageable) {
        return productRepository.findProductsByCategory(category, userId, pageable);
    }

    /*
     * 상품 상세 정보 조회
     */
    public ProductDetailsResponse getProductDetails(Long productId, Long userId) {
        return productRepository.findProductDetailsById(productId, userId)
                .orElseThrow(() -> new ProductException(PRODUCT_NOT_FOUND));
    }

    /*
     * 나의 사전 등록 상품 목록 조회
     */
    public Page<ProductResponse> getMyProductList(String nickname, Pageable pageable) {
        return productRepository.findProductsByNickname(nickname, pageable);
    }

    /*
     * 상품 카테고리 목록 조회
     */
    public List<CategoryResponse> getCategories() {
        return Arrays.stream(Product.Category.values())
                .map(category -> new CategoryResponse(category.name(), category.getDisplayName()))
                .toList();
    }

    /*
     * 사전 등록 상품 수정
     */
    @Transactional
    public UpdateProductResponse updateProduct(Long productId, UpdateProductRequest request, List<MultipartFile> images) {
        logger.info("상품 ID {}번에 대한 사전 등록 정보를 업데이트를 시작합니다.", productId);
        // 상품 유효성 검사
        Product existingProduct = productRepository.findByIdAndUserId(productId, request.getUserId())
                .orElseThrow(() -> {
                    logger.info("유저 ID {}번 유저가 등록한 상품 ID {}번에 해당하는 사전 등록 상품을 찾을 수 없습니다.", request.getUserId(), productId);
                    return new ProductException(PRODUCT_NOT_FOUND);
                });

        // 경매 등록 상태 유무 유효성 검사
        if (auctionRepository.existsByProductId(productId)) {
            logger.info("상품 ID {}번에 해당하는 상품은 이미 경매 등록 상태입니다.", productId);
            throw new ProductException(ALREADY_IN_AUCTION);
        }

        // 상품 정보 업데이트
        existingProduct.update(request);

        // 이미지 저장
        if (images != null && !images.isEmpty()) {
            updateProductImages(existingProduct, images);
        }

        logger.info("상품 ID {}번에 대한 사전 등록 정보를 업데이트를 완료했습니다.", productId);
        return UpdateProductResponse.from(existingProduct);
    }

    /**
     * 상품 이미지 업데이트
     */
    public void updateProductImages(Product product, List<MultipartFile> newImages) {
        List<Image> existingImages = product.getImages();
        // 기존 이미지 URL 추출
        List<String> currentImageUrls = existingImages.stream()
                .map(Image::getCdnPath)
                .toList();

        // S3에 기존 이미지 삭제
        imageService.deleteUploadImages(currentImageUrls);
        // DB에 기존 이미지 엔티티 삭제
        imageRepository.deleteAll(existingImages);
        // 상품 객체에서 이미지 리스트 초기화
        product.clearImages();
        logger.info("상품 ID {}번의 기존 이미지 삭제를 모두 마쳤습니다.", product.getId());

        // S3에 새 이미지 업로드
        List<String> newImageUrls = imageService.uploadImages(newImages);
        // DB에 새 이미지 엔티티 저장
        List<Image> newImageEntities = imageService.saveProductImageEntities(product, newImageUrls);
        // 상품 객체에 이미지 추가
        product.addImages(newImageEntities);
        logger.info("상품 ID {}번의 이미지를 성공적으로 저장하였습니다.", product.getId());
    }

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
