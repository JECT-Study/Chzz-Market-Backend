package org.chzz.market.domain.product.service;

import lombok.RequiredArgsConstructor;
import org.chzz.market.domain.auction.repository.AuctionRepository;
import org.chzz.market.domain.image.entity.Image;
import org.chzz.market.domain.image.repository.ImageRepository;
import org.chzz.market.domain.image.service.ImageService;
import org.chzz.market.domain.product.dto.UpdateProductRequest;
import org.chzz.market.domain.product.dto.UpdateProductResponse;
import org.chzz.market.domain.product.entity.Product;
import org.chzz.market.domain.product.error.ProductException;
import org.chzz.market.domain.product.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static org.chzz.market.domain.product.error.ProductErrorCode.ALREADY_IN_AUCTION;
import static org.chzz.market.domain.product.error.ProductErrorCode.PRODUCT_NOT_FOUND;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ProductService {

    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);

    private final ProductRepository productRepository;
    private final AuctionRepository auctionRepository;
    private final ImageService imageService;
    private final ImageRepository imageRepository;

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
}
