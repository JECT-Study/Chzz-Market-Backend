package org.chzz.market.domain.auction.service;

import org.chzz.market.domain.auction.dto.request.BaseRegisterRequest;
import org.chzz.market.domain.auction.dto.request.StartAuctionRequest;
import org.chzz.market.domain.auction.dto.response.*;
import org.chzz.market.domain.auction.entity.Auction;
import org.chzz.market.domain.auction.service.policy.AuctionPolicy;

import org.chzz.market.domain.auction.error.AuctionException;
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

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;

import org.chzz.market.domain.product.entity.Product.Category;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static org.chzz.market.domain.auction.dto.request.BaseRegisterRequest.AuctionType.*;
import static org.chzz.market.domain.auction.error.AuctionErrorCode.AUCTION_NOT_FOUND;
import org.springframework.web.multipart.MultipartFile;

import static org.chzz.market.domain.auction.error.AuctionErrorCode.*;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AuctionService {

    private static final Logger logger = LoggerFactory.getLogger(AuctionService.class);

    private final AuctionRepository auctionRepository;
    private final ProductRepository productRepository;
    private final ImageService imageService;
    private final UserRepository userRepository;

    /**
     * 상품 등록 (사전 등록 & 경매 등록)
     */
    @Transactional
    public RegisterResponse registerAuction(BaseRegisterRequest request, List<MultipartFile> images) {
        // 유저 유효성 검사
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        AuctionPolicy auctionPolicy = request.getAuctionType().getAuctionPolicy();

        // 상품 생성
        Product product = auctionPolicy.createProduct(request, user);
        productRepository.save(product);
        logger.info("상품이 상품 테이블에 저장되었습니다. 상품 ID : {}", product.getId());

        if (images != null && !images.isEmpty()) {
            List<String> imageUrls = imageService.uploadImages(images);
            imageService.saveProductImageEntities(product, imageUrls);
        }

        // 경매 등록인 경우만 경매 테이블 저장
        if (request.getAuctionType() == REGISTER) {
            Auction auction = auctionPolicy.createAuction(product, request);
            auctionRepository.save(auction);
            logger.info("상품이 경매 테이블에 저장되었습니다. 최종 경매 상태 : {}", auction.getStatus());
            return RegisterAuctionResponse.of(product.getId(), auction.getId(), auction.getStatus());
        } else {
            return PreRegisterResponse.of(product.getId());
        }
    }

    public Auction getAuction(Long auctionId) {
        return auctionRepository.findById(auctionId)
                .orElseThrow(() -> new AuctionException(AUCTION_NOT_FOUND));
    }

    public Page<AuctionResponse> getAuctionListByCategory(Category category, Long userId,
                                                          Pageable pageable) {
        return auctionRepository.findAuctionsByCategory(category, userId, pageable);
    }

    public AuctionDetailsResponse getAuctionDetails(Long auctionId, Long userId) {
        Optional<AuctionDetailsResponse> auctionDetails = auctionRepository.findAuctionDetailsById(auctionId, userId);
        return auctionDetails.orElseThrow(() -> new AuctionException(AUCTION_NOT_ACCESSIBLE));
    }

    public Product validateStartAuction(Long productId) {
        logger.info("사전 등록 상품 유효성 검사를 시작합니다. 상품 ID: {}", productId);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new AuctionException(AUCTION_NOT_FOUND));

        // 이미 경매로 등록된 상품인지 유효성 검사
        if (auctionRepository.existsByProductId(product.getId())) {
            throw new AuctionException(AUCTION_ALREADY_REGISTERED);
        }

        logger.info("유효성 검사가 끝났습니다. 상품 ID : {}", productId);
        return product;
    }

    @Transactional
    public StartAuctionResponse changeAuction(Product product) {
        logger.info("사전 등록 상품을 경매 등록 상품으로 전환하기 시작합니다. 상품 ID: {}", product.getId());

        Auction auction = Auction.toEntity(product);
        auction = auctionRepository.save(auction);
        logger.info("경매가 시작되었습니다. 등록된 경매 마감 시간 : {}", auction.getEndDateTime());

        return StartAuctionResponse.of(
                auction.getId(),
                auction.getProduct().getId(),
                auction.getStatus(),
                auction.getEndDateTime()
        );
    }

    /**
     * 사전 등록 상품 경매 전환 처리
     * TODO: 추후에 인증된 사용자 정보로 수정 필요
     */
    @Transactional
    public StartAuctionResponse startAuction(StartAuctionRequest request) {
        Product product = validateStartAuction(request.getProductId());
        return changeAuction(product);
    }

}