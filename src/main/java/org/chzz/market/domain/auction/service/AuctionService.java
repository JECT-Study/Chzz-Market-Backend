package org.chzz.market.domain.auction.service;

import org.chzz.market.domain.auction.dto.*;
import org.chzz.market.domain.auction.entity.Auction;
import org.chzz.market.domain.user.error.exception.UserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;

import org.chzz.market.domain.auction.error.AuctionException;
import org.chzz.market.domain.auction.entity.Auction.AuctionStatus;
import org.chzz.market.domain.auction.entity.SortType;
import org.chzz.market.domain.auction.repository.AuctionRepository;
import org.chzz.market.domain.product.entity.Product.Category;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static java.time.LocalDateTime.*;
import static org.chzz.market.domain.auction.error.AuctionErrorCode.*;
import static org.chzz.market.domain.user.error.UserErrorCode.USER_NOT_MATCHED;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AuctionService {

    private static final Logger logger = LoggerFactory.getLogger(AuctionService.class);

    private final AuctionRepository auctionRepository;

    /**
     * 사전 등록 상품 경매 전환 처리
     * TODO: 추후에 인증된 사용자 정보로 수정 필요
     */
    @Transactional
    public StartResponse startAuction(Long auctionId, Long userId) {
        logger.info("사전 등록 상품을 경매 등록 상품으로 전환하기 시작합니다. 경매 ID: {}", auctionId);
        // 상품 유효성 검사
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> {
                    logger.info("경매 ID {}번에 해당하는 경매를 찾을 수 없습니다.", auctionId);
                    return new AuctionException(AUCTION_NOT_FOUND);
                });

        // 사용자 유효성 검사
        if (!auction.getProduct().getUser().getId().equals(userId)){
            throw new UserException(USER_NOT_MATCHED);
        }

        // 상품 상태 유효성 검사
        if (auction.getStatus() != AuctionStatus.PENDING) {
            throw new AuctionException(INVALID_AUCTION_STATE);
        }

        logger.info("유효성 검사가 끝났습니다. 초기 경매 상태 : {}", auction.getStatus());

        LocalDateTime endTime = now().plusHours(24);
        logger.info("경매 마감 시간 : {}", endTime);

        // 상품 경매 시작 처리 및 저장
        auction.start(endTime);
        logger.info("경매가 시작되었습니다. 현재 경매 상태 : {}", auction.getStatus());
        Auction savedAuction = auctionRepository.save(auction);
        logger.info("경매 상품이 저장되었습니다. 최종 경매 상태 : {}", auction.getStatus());

        return StartResponse.of(
                savedAuction.getId(),
                savedAuction.getProduct().getId(),
                savedAuction.getStatus(),
                savedAuction.getEndDateTime()
        );
    }

    public Auction getAuction(Long auctionId) {
        return auctionRepository.findById(auctionId)
                .orElseThrow(() -> new AuctionException(AUCTION_NOT_FOUND));
    }

    public Page<AuctionResponse> getAuctionListByCategory(Category category, SortType sortType, Long userId,
                                                      Pageable pageable) {
        return auctionRepository.findAuctionsByCategory(category, sortType, userId, pageable);
    }

    public AuctionDetailsResponse getAuctionDetails(Long auctionId, Long userId) {
        Optional<AuctionDetailsResponse> auctionDetails = auctionRepository.findAuctionDetailsById(auctionId, userId);
        return auctionDetails.orElseThrow(() -> new AuctionException(AUCTION_NOT_ACCESSIBLE));
    }

}
