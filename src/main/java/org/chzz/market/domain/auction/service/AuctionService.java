package org.chzz.market.domain.auction.service;

import static org.chzz.market.domain.auction.error.AuctionErrorCode.*;
import static org.chzz.market.domain.notification.entity.NotificationType.AUCTION_FAILURE;
import static org.chzz.market.domain.notification.entity.NotificationType.AUCTION_NON_WINNER;
import static org.chzz.market.domain.notification.entity.NotificationType.AUCTION_SUCCESS;
import static org.chzz.market.domain.notification.entity.NotificationType.AUCTION_WINNER;
import static org.chzz.market.domain.product.error.ProductErrorCode.FORBIDDEN_PRODUCT_ACCESS;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.chzz.market.domain.auction.dto.request.StartAuctionRequest;
import org.chzz.market.domain.auction.dto.response.AuctionDetailsResponse;
import org.chzz.market.domain.auction.dto.response.AuctionResponse;
import org.chzz.market.domain.auction.dto.response.LostAuctionResponse;
import org.chzz.market.domain.auction.dto.response.SimpleAuctionResponse;
import org.chzz.market.domain.auction.dto.response.StartAuctionResponse;
import org.chzz.market.domain.auction.dto.response.UserAuctionResponse;
import org.chzz.market.domain.auction.dto.response.WonAuctionResponse;
import org.chzz.market.domain.auction.entity.Auction;
import org.chzz.market.domain.auction.error.AuctionException;
import org.chzz.market.domain.auction.repository.AuctionRepository;
import org.chzz.market.domain.bid.entity.Bid;
import org.chzz.market.domain.bid.service.BidService;
import org.chzz.market.domain.image.entity.Image;
import org.chzz.market.domain.notification.event.NotificationEvent;
import org.chzz.market.domain.product.entity.Product;
import org.chzz.market.domain.product.entity.Product.Category;
import org.chzz.market.domain.product.error.ProductErrorCode;
import org.chzz.market.domain.product.error.ProductException;
import org.chzz.market.domain.product.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
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
    private final AuctionRepository auctionRepository;
    private final ProductRepository productRepository;
    private final ApplicationEventPublisher eventPublisher;

    /*
     * 경매 ID로 경매 정보를 조회합니다.
     */
    public Auction getAuction(Long auctionId) {
        return auctionRepository.findById(auctionId)
                .orElseThrow(() -> new AuctionException(AUCTION_NOT_FOUND));
    }

    /*
     * 카테고리에 따라 경매 리스트를 조회합니다.
     */
    public Page<AuctionResponse> getAuctionListByCategory(Category category, Long userId, Pageable pageable) {
        return auctionRepository.findAuctionsByCategory(category, userId, pageable);
    }

    /*
     * 경매 상세 정보를 조회합니다.
     * TODO: 서비스 추상화 적용 시 참고 (#9 관련)
     * 현재 enum 통해 응답 형태 다양화 구현
     * 추후 서비스 추상화 적용 시 이 부분 활용해 구현할 수 있습니다.
     */
    public AuctionDetailsResponse getFullAuctionDetails(Long auctionId, Long userId) {
        return auctionRepository.findAuctionDetailsById(auctionId, userId)
                .orElseThrow(() -> new AuctionException(AUCTION_NOT_ACCESSIBLE));
    }

    /*
     * 판매자 입찰 화면에 제공되는 경매 간단 상세 정보를 조회합니다.
     */
    public SimpleAuctionResponse getSimpleAuctionDetails(Long auctionId) {
        return auctionRepository.findSimpleAuctionDetailsById(auctionId)
                .orElseThrow(() -> new AuctionException(AUCTION_NOT_FOUND));
    }

    /*
     * 사용자 닉네임에 따라 경매 리스트를 조회합니다.
     */
    public Page<UserAuctionResponse> getAuctionListByNickname(String nickname, Pageable pageable) {
        return auctionRepository.findAuctionsByNickname(nickname, pageable);
    }

    /*
     * 사용자가 참여한(입찰한) 경매 상세 정보를 조회합니다.
     */
    public Page<AuctionResponse> getAuctionHistory(Long userId, Pageable pageable) {
        return auctionRepository.findParticipatingAuctionRecord(userId, pageable);
    }

    /*
     * 내가 성공한 경매 조회
     */
    public Page<WonAuctionResponse> getWonAuctionHistory(Long userId, Pageable pageable) {
        return auctionRepository.findWonAuctionHistoryByUserId(userId, pageable);
    }

    /*
     * 내가 실패한 경매 조회
     */
    public Page<LostAuctionResponse> getLostAuctionHistory(Long userId, Pageable pageable) {
        return auctionRepository.findLostAuctionHistoryByUserId(userId, pageable);
    }

    /**
     * 사전 등록 상품 경매 전환 처리
     */
    @Transactional
    public StartAuctionResponse startAuction(Long userId, StartAuctionRequest request) {
        Product product = validateStartAuction(request.getProductId(), userId);
        return changeAuction(product);
    }

    /*
     * 사전 등록 상품 유효성 검사
     */
    public Product validateStartAuction(Long productId, Long userId) {
        logger.info("사전 등록 상품 유효성 검사를 시작합니다. 상품 ID: {}", productId);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductException(ProductErrorCode.PRODUCT_NOT_FOUND));

        // 등록된 상품의 사용자 정보와 전환 요청한 사용자 정보 유효성 검사
        if (!product.isOwner(userId)) {
            throw new ProductException(FORBIDDEN_PRODUCT_ACCESS);
        }

        // 이미 경매로 등록된 상품인지 유효성 검사
        if (auctionRepository.existsByProductId(product.getId())) {
            throw new AuctionException(AUCTION_ALREADY_REGISTERED);
        }

        logger.info("유효성 검사가 끝났습니다. 상품 ID : {}", productId);
        return product;
    }

    /*
     * 경매 상품으로 전환
     */
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

    /*
     * 경매 입찰 내역 조회
     */
    public List<AuctionResponse> getBestAuctionList() {
        return auctionRepository.findBestAuctions();
    }

    /*
     * 경매 종료까지 1시간 이내인 경매 조회
     */
    public List<AuctionResponse> getImminentAuctionList() {
        return auctionRepository.findImminentAuctions();

    }

    /*
     * 경매 종료 처리
     */
    @Transactional
    public void completeAuction(Long auctionId) {
        logger.info("경매 종료 작업 시작 auction ID: {}", auctionId);
        Auction auction = getAuction(auctionId);
        auction.endAuction();
        processAuctionResults(auction);
    }

    /*
     * 경매 결과 처리
     */
    private void processAuctionResults(Auction auction) {
        Long productUserId = auction.getProduct().getUser().getId();
        String productName = auction.getProduct().getName();
        Image firstImage = auction.getProduct().getFirstImage().orElse(null);
        List<Bid> bids = bidService.findAllBidsByAuction(auction);
        if (bids.isEmpty()) { // 입찰이 없는 경우
            eventPublisher.publishEvent(
                    NotificationEvent.of(productUserId, AUCTION_FAILURE, AUCTION_FAILURE.getMessage(productName),
                            firstImage)); // 낙찰 실패 알림 이벤트
            return;
        }
        eventPublisher.publishEvent(
                NotificationEvent.of(productUserId, AUCTION_SUCCESS, AUCTION_SUCCESS.getMessage(productName),
                        firstImage)); // 낙찰 성공 알림 이벤트
        processWinningBid(auction, bids.get(0), productName, firstImage); // 첫 번째 입찰이 낙찰
        processNonWinningBids(bids, productName, firstImage);

    }

    // 낙찰자 처리
    private void processWinningBid(Auction auction, Bid winningBid, String productName, Image firstImage) {
        auction.assignWinner(winningBid.getBidder().getId());
        eventPublisher.publishEvent(NotificationEvent.of(winningBid.getBidder().getId(), AUCTION_WINNER,
                AUCTION_WINNER.getMessage(productName), firstImage)); // 낙찰자 알림 이벤트
        log.info("경매 ID {}: 낙찰자 처리 완료", auction.getId());
    }

    // 미낙찰자 처리
    private void processNonWinningBids(List<Bid> bids, String productName, Image firstImage) {
        List<Long> nonWinnerIds = bids.stream().skip(1) // 낙찰자를 제외한 나머지 입찰자들
                .map(bid -> bid.getBidder().getId()).collect(Collectors.toList());

        if (!nonWinnerIds.isEmpty()) {
            eventPublisher.publishEvent(NotificationEvent.of(nonWinnerIds, AUCTION_NON_WINNER,
                    AUCTION_NON_WINNER.getMessage(productName), firstImage)); // 미낙찰자 알림 이벤트
        }
    }
}
