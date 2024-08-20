package org.chzz.market.domain.auction.service;

import static org.chzz.market.domain.auction.error.AuctionErrorCode.AUCTION_ALREADY_REGISTERED;
import static org.chzz.market.domain.auction.error.AuctionErrorCode.AUCTION_NOT_ACCESSIBLE;
import static org.chzz.market.domain.auction.error.AuctionErrorCode.AUCTION_NOT_FOUND;
import static org.chzz.market.domain.notification.entity.Notification.Type.AUCTION_FAILURE;
import static org.chzz.market.domain.notification.entity.Notification.Type.AUCTION_NON_WINNER;
import static org.chzz.market.domain.notification.entity.Notification.Type.AUCTION_SUCCESS;
import static org.chzz.market.domain.notification.entity.Notification.Type.AUCTION_WINNER;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.chzz.market.domain.auction.dto.request.BaseRegisterRequest;
import org.chzz.market.domain.auction.dto.request.RegisterAuctionRequest;
import org.chzz.market.domain.auction.dto.request.StartAuctionRequest;
import org.chzz.market.domain.auction.dto.response.AuctionDetailsResponse;
import org.chzz.market.domain.auction.dto.response.AuctionResponse;
import org.chzz.market.domain.auction.dto.response.MyAuctionResponse;
import org.chzz.market.domain.auction.dto.response.RegisterAuctionResponse;
import org.chzz.market.domain.auction.dto.response.StartAuctionResponse;
import org.chzz.market.domain.auction.entity.Auction;
import org.chzz.market.domain.auction.error.AuctionException;
import org.chzz.market.domain.auction.repository.AuctionRepository;
import org.chzz.market.domain.auction.service.policy.AuctionPolicy;
import org.chzz.market.domain.bid.entity.Bid;
import org.chzz.market.domain.bid.service.BidService;
import org.chzz.market.domain.image.service.ImageService;
import org.chzz.market.domain.notification.dto.NotificationMessage;
import org.chzz.market.domain.notification.service.NotificationService;
import org.chzz.market.domain.product.entity.Product;
import org.chzz.market.domain.product.entity.Product.Category;
import org.chzz.market.domain.product.repository.ProductRepository;
import org.chzz.market.domain.user.entity.User;
import org.chzz.market.domain.user.error.UserErrorCode;
import org.chzz.market.domain.user.error.exception.UserException;
import org.chzz.market.domain.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class AuctionService {

    private static final Logger logger = LoggerFactory.getLogger(AuctionService.class);

    private final BidService bidService;
    private final ImageService imageService;
    private final NotificationService notificationService;
    private final ProductRepository productRepository;
    private final AuctionRepository auctionRepository;
    private final UserRepository userRepository;

    /**
     * 상품 등록 (사전 등록 & 경매 등록)
     */
    @Transactional
    public RegisterAuctionResponse registerAuction(BaseRegisterRequest request, List<MultipartFile> images) {
        // 유저 유효성 검사
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> {
                    logger.info("유저 ID {}번에 해당하는 유저를 찾을 수 없습니다.", request.getUserId());
                    return new UserException(UserErrorCode.USER_NOT_FOUND);
                });

        Product product;
        Auction auction = null;
        AuctionPolicy auctionPolicy = request.getAuctionType().getAuctionPolicy();

        // 상품 생성
        product = auctionPolicy.createProduct(request, user);
        productRepository.save(product);
        logger.info("상품이 상품 테이블에 저장되었습니다. 상품 ID : {}", product.getId());

        // 경매 생성 (사전 등록일 경우 저장 X)
        if (request instanceof RegisterAuctionRequest) {
            auction = auctionPolicy.createAuction(product, request);
            auctionRepository.save(auction);
            logger.info("상품이 경매 테이블에 저장되었습니다. 최종 경매 상태 : {}", auction.getStatus());
        }

        if (images != null && !images.isEmpty()) {
            List<String> imageUrls = imageService.uploadImages(images);
            imageService.saveProductImageEntities(product, imageUrls);
        }

        return RegisterAuctionResponse.of(
                product.getId(),
                auction != null ? auction.getId() : null,
                auction != null ? auction.getStatus() : null
        );
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

    public Page<MyAuctionResponse> getAuctionListByUserId(Long userId, Pageable pageable) {
        return auctionRepository.findAuctionsByUserId(userId, pageable);
    }

    /**
     * 사전 등록 상품 경매 전환 처리
     * TODO: 추후에 인증된 사용자 정보로 수정 필요
     */
    @Transactional
    public StartAuctionResponse startAuction(StartAuctionRequest request) {
        logger.info("사전 등록 상품을 경매 등록 상품으로 전환하기 시작합니다. 상품 ID: {}", request.getProductId());
        // 상품 유효성 검사
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> {
                    logger.info("상품 ID {}번에 해당하는 경매를 찾을 수 없습니다.", request.getProductId());
                    return new AuctionException(AUCTION_NOT_FOUND);
                });

        // 이미 경매로 등록된 상품인지 유효성 검사
        if (auctionRepository.existsByProductId(product.getId())) {
            throw new AuctionException(AUCTION_ALREADY_REGISTERED);
        }

        logger.info("유효성 검사가 끝났습니다. 상품 ID : {}", product.getId());

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

    @Transactional
    public void completeAuction(Long auctionId) {
        logger.info("경매 종료 작업 시작 auction ID: {}", auctionId);
        Auction auction = getAuction(auctionId);
        auction.endAuction();
        processAuctionResults(auction);
    }

    private void processAuctionResults(Auction auction) {
        String productName = auction.getProduct().getName();
        Long productUserId = auction.getProduct().getUser().getId();
        List<Bid> bids = bidService.findAllBidsByAuction(auction);

        if (bids.isEmpty()) { // 입찰이 없는 경우
            notifyAuctionFailure(productUserId, productName);
            return;
        }
        notifyAuctionSuccess(productUserId, productName);

        log.info("경매 ID {}: 낙찰 계산", auction.getId());
        Bid winningBid = bids.get(0); // 첫 번째 입찰이 낙찰
        auction.assignWinner(winningBid.getBidder().getId());

        notifyAuctionWinner(winningBid.getBidder().getId(), productName);
        notifyNonWinners(bids, productName);
    }

    // 판매자에게 미 낙찰 알림
    private void notifyAuctionFailure(Long productUserId, String productName) {
        notificationService.sendNotification(
                new NotificationMessage(productUserId, AUCTION_FAILURE, productName)
        );
    }

    // 판매자에게 낙찰 알림
    private void notifyAuctionSuccess(Long productUserId, String productName) {
        notificationService.sendNotification(
                new NotificationMessage(productUserId, AUCTION_SUCCESS, productName)
        );
    }

    // 낙찰자에게 알림
    private void notifyAuctionWinner(Long winnerId, String productName) {
        notificationService.sendNotification(
                new NotificationMessage(winnerId, AUCTION_WINNER, productName)
        );
    }

    // 미낙찰자들에게 알림
    private void notifyNonWinners(List<Bid> bids, String productName) {
        List<Long> nonWinnerIds = bids.stream()
                .skip(1) // 낙찰자를 제외한 나머지 입찰자들
                .map(bid -> bid.getBidder().getId())
                .collect(Collectors.toList());

        if (!nonWinnerIds.isEmpty()) {
            notificationService.sendNotification(
                    new NotificationMessage(nonWinnerIds, AUCTION_NON_WINNER, productName)
            );
        }
    }
}
