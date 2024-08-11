package org.chzz.market.domain.auction.service;

import lombok.RequiredArgsConstructor;
import org.chzz.market.domain.auction.dto.RegisterRequest;
import org.chzz.market.domain.auction.dto.RegisterResponse;
import org.chzz.market.domain.auction.entity.Auction;
import org.chzz.market.domain.auction.repository.AuctionRepository;
import org.chzz.market.domain.image.service.ImageService;
import org.chzz.market.domain.product.entity.Product;
import org.chzz.market.domain.product.repository.ProductRepository;
import org.chzz.market.domain.user.entity.User;
import org.chzz.market.domain.user.error.UserErrorCode;
import org.chzz.market.domain.user.error.exception.UserException;
import org.chzz.market.domain.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.chzz.market.domain.auction.entity.Auction.*;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RegisterServiceImpl implements RegisterService{

    private static final Logger logger = LoggerFactory.getLogger(RegisterServiceImpl.class);

    private final ProductRepository productRepository;
    private final AuctionRepository auctionRepository;
    private final ImageService imageService;
    private final UserRepository userRepository;

    /**
     * 상품 등록 (사전 등록 & 경매 등록)
     */
    @Override
    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        // 유저 유효성 검사
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> {
                    logger.info("유저 ID {}번에 해당하는 유저를 찾을 수 없습니다.", request.getUserId());
                    return new UserException(UserErrorCode.USER_NOT_FOUND);
                });

        // 상품 테이블 등록
        // Product product = request.toProductEntity();
        Product product = createProduct(request, user);
        productRepository.save(product);
        logger.info("상품이 상품 테이블에 저장되었습니다. 상품 ID : {}", product.getId());

        // 경매 테이블 등록
        // Auction auction = request.toAuctionEntity(product, initialStatus);
        Auction auction = createAuction(product, request, request.getStatus());
        auctionRepository.save(auction);
        logger.info("상품이 경매 테이블에 저장되었습니다. 최종 경매 상태 : {}", auction.getStatus());

        // 이미지 처리
        if (request.getImages() != null && !request.getImages().isEmpty()) {
            List<String> imageUrls = imageService.uploadImages(request.getImages());
            imageService.saveProductImageEntities(product, imageUrls);
        }

        return RegisterResponse.of(product.getId(), auction.getId(), auction.getStatus());
    }

    // TODO: createProduct, createAuction 메서드 추후 로그인 기능 병합 이후 toEntity 로 수정
    private Product createProduct(RegisterRequest request, User user) {
        return Product.builder()
                .user(user)
                .name(request.getProductName())
                .description(request.getDescription())
                .category(request.getCategory())
                .build();
    }

    private Auction createAuction(Product product, RegisterRequest request, AuctionStatus status) {
        return Auction.builder()
                .product(product)
                .minPrice(request.getMinPrice())
                .status(status)
                .build();
    }

}
