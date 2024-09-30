package org.chzz.market.domain.auction.repository;

import java.util.List;
import java.util.Optional;

import org.chzz.market.domain.auction.dto.response.*;
import org.chzz.market.domain.product.entity.Product.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AuctionRepositoryCustom {
    /**
     * 카테고리에 따라 경매 리스트를 조회합니다.
     *
     * @param category 카테고리
     * @param userId   사용자 ID
     * @param pageable 페이징 정보
     * @return 페이징된 경매 응답 리스트
     */
    Page<AuctionResponse> findAuctionsByCategory(Category category, Long userId, Pageable pageable);

    /**
     * 사용자가 참여한(입찰한) 경매 상세 정보를 조회합니다.
     * @param userId - 사용자 ID
     * @param pageable - 페이징 정보
     * @return - 페이징된 경매 응답 리스트
     */
    Page<AuctionResponse> findParticipatingAuctionRecord(Long userId, Pageable pageable);

    /**
     * 경매 ID와 사용자 ID로 경매 상세 정보를 조회합니다.
     *
     * @param auctionId 경매 ID
     * @param userId    사용자 ID
     * @return 경매 상세 응답
     */
    Optional<AuctionDetailsResponse> findAuctionDetailsById(Long auctionId, Long userId);

    /**
     * 경매 ID로 경매 간단 상세 정보를 조회합니다.
     * @param auctionId 경매 ID
     * @return          경매 간단 상세정보 응답
     */
    Optional<SimpleAuctionResponse> findSimpleAuctionDetailsById(Long auctionId);

    /**
     * 사용자 닉네임에 따라 경매 리스트를 조회합니다.
     *
     * @param nickname 사용자 닉네임
     * @param pageable 페이징 정보
     * @return 페이징된 사용자 경매 응답 리스트
     */
    Page<UserAuctionResponse> findAuctionsByNickname(String nickname, Pageable pageable);

    /**
     * @param userId 사용자 ID
     * @param pageable 페이징 정보
     * @return 페이징된 사용자 경매 등록 기록
     */
    Page<UserAuctionResponse> findAuctionsByUserId(Long userId, Pageable pageable);

    /**
     * 홈 화면의 베스트 경매 조회
     * @return 입찰 기록이 많은 경매 정보
     */
    List<AuctionResponse> findBestAuctions();

    /**
     * 홈 화면의 임박 경매 조회
     * @return 경매 종료까지 1시간 이내인 경매 정보
     */
    List<AuctionResponse> findImminentAuctions();

    /**
     * 사용자의 참여 횟수, 낙찰 횟수, 낙찰 실패 횟수를 조회합니다.
     * @param userId 사용자 ID
     * @return 참여 횟수, 낙찰 횟수, 낙찰 실패 횟수 응답
     */
    List<AuctionParticipationResponse> getAuctionParticipations(Long userId);

    /**
     * 사용자가 낙찰한 경매 이력을 조회합니다.
     * @param userId    사용자 ID
     * @param pageable  페이징 정보
     * @return          페이징된 낙찰 경매 응답 리스트
     */
    Page<WonAuctionResponse> findWonAuctionHistoryByUserId(Long userId, Pageable pageable);

    /**
     * 사용자가 낙찰하지 못한 경매 이력을 조회합니다.
     * @param userId   사용자 ID
     * @param pageable 페이징 정보
     * @return         페이징된 낙찰 실패 경매 응답 리스트
     */
    Page<LostAuctionResponse> findLostAuctionHistoryByUserId(Long userId, Pageable pageable);
}
