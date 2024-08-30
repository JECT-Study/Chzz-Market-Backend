package org.chzz.market.domain.auction.service;

import static org.chzz.market.domain.auction.error.AuctionErrorCode.AUCTION_ALREADY_REGISTERED;
import static org.chzz.market.domain.auction.error.AuctionErrorCode.AUCTION_NOT_ACCESSIBLE;
import static org.chzz.market.domain.auction.error.AuctionErrorCode.AUCTION_NOT_FOUND;
import static org.chzz.market.domain.auction.error.AuctionErrorCode.UNAUTHORIZED_AUCTION;
import static org.chzz.market.domain.notification.entity.Notification.Type.AUCTION_FAILURE;
import static org.chzz.market.domain.notification.entity.Notification.Type.AUCTION_NON_WINNER;
import static org.chzz.market.domain.notification.entity.Notification.Type.AUCTION_SUCCESS;
import static org.chzz.market.domain.notification.entity.Notification.Type.AUCTION_WINNER;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.chzz.market.domain.auction.dto.request.StartAuctionRequest;
import org.chzz.market.domain.auction.dto.response.AuctionDetailsResponse;
import org.chzz.market.domain.auction.dto.response.AuctionResponse;
import org.chzz.market.domain.auction.dto.response.MyAuctionResponse;
import org.chzz.market.domain.auction.dto.response.StartAuctionResponse;
import org.chzz.market.domain.auction.entity.Auction;
import org.chzz.market.domain.auction.error.AuctionException;
import org.chzz.market.domain.auction.repository.AuctionRepository;
import org.chzz.market.domain.bid.entity.Bid;
import org.chzz.market.domain.bid.service.BidService;
import org.chzz.market.domain.notification.dto.NotificationMessage;
import org.chzz.market.domain.notification.service.NotificationService;
import org.chzz.market.domain.product.entity.Product;
import org.chzz.market.domain.product.entity.Product.Category;
import org.chzz.market.domain.product.repository.ProductRepository;
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
    private final NotificationService notificationService;
    private final AuctionRepository auctionRepository;
    private final ProductRepository productRepository;

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
        Product product = validateStartAuction(request.getProductId(), request.getUserId());
        return changeAuction(product);
    }

    public Product validateStartAuction(Long productId, Long userId) {
        logger.info("사전 등록 상품 유효성 검사를 시작합니다. 상품 ID: {}", productId);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new AuctionException(AUCTION_NOT_FOUND));

        // 등록된 상품의 사용자 정보와 전환 요청한 사용자 정보 유효성 검사
        if (!product.getUser().getId().equals(userId)) {
            throw new AuctionException(UNAUTHORIZED_AUCTION);
        }

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
