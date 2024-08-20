package org.chzz.market.domain.auction.service;

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
import org.chzz.market.domain.auction.dto.request.AuctionCreateRequest;
import org.chzz.market.domain.auction.dto.response.AuctionDetailsResponse;
import org.chzz.market.domain.auction.dto.response.AuctionResponse;
import org.chzz.market.domain.auction.dto.response.MyAuctionResponse;
import org.chzz.market.domain.auction.entity.Auction;
import org.chzz.market.domain.auction.error.AuctionException;
import org.chzz.market.domain.auction.repository.AuctionRepository;
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
import org.chzz.market.domain.user.error.UserException;
import org.chzz.market.domain.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
    public Long createAuction(AuctionCreateRequest dto) {

        // 사용자 데이터 조회
        User seller = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        // 상품 데이터 저장
        Product product = Product.builder()
                .name(dto.getTitle())
                .description(dto.getDescription())
                .category(dto.getCategory())
                .user(seller)
                .build();
        product = productRepository.save(product);

        // 경매 데이터 저장
        Auction auction = Auction.builder()
                .product(product)
                .minPrice(dto.getMinPrice())
                .status(dto.isPreOrder() ? Auction.Status.PENDING : Auction.Status.PROCEEDING)
                .build();
        auction = auctionRepository.save(auction);

        // 이미지 처리
        List<String> cdnPaths = imageService.saveProductImages(product, dto.getImages());

        // 이미지 URL Logging
        cdnPaths.forEach(path -> logger.info("Uploaded image path: {}", imageService.getFullImageUrl(path)));

        return auction.getId();
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
