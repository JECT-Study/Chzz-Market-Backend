package org.chzz.market.domain.auction.repository;

import java.util.List;
import java.util.Optional;
import org.chzz.market.domain.auction.dto.response.AuctionDetailsResponse;
import org.chzz.market.domain.auction.dto.response.AuctionResponse;
import org.chzz.market.domain.auction.dto.response.LostAuctionResponse;
import org.chzz.market.domain.auction.dto.response.SimpleAuctionResponse;
import org.chzz.market.domain.auction.dto.response.UserAuctionResponse;
import org.chzz.market.domain.auction.dto.response.UserEndedAuctionResponse;
import org.chzz.market.domain.auction.dto.response.WonAuctionDetailsResponse;
import org.chzz.market.domain.auction.dto.response.WonAuctionResponse;
import org.chzz.market.domain.product.entity.Product.Category;
import org.chzz.market.domain.user.dto.response.ParticipationCountsResponse;
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
     * 경매 ID와 사용자 ID로 경매 상세 정보를 조회합니다.
     *
     * @param auctionId 경매 ID
     * @param userId    사용자 ID
     * @return 경매 상세 응답
     */
    Optional<AuctionDetailsResponse> findAuctionDetailsById(Long auctionId, Long userId);

    /**
     * 경매 ID로 경매 간단 상세 정보를 조회합니다.
     *
     * @param auctionId 경매 ID
     * @return 경매 간단 상세정보 응답
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
     * @param userId   사용자 ID
     * @param pageable 페이징 정보
     * @return 페이징된 사용자 경매 등록 기록
     */
    Page<UserAuctionResponse> findAuctionsByUserId(Long userId, Pageable pageable);

    /**
     * 홈 화면의 베스트 경매 조회
     *
     * @return 입찰 기록이 많은 경매 정보
     */
    List<AuctionResponse> findBestAuctions();

    /**
     * 홈 화면의 임박 경매 조회
     *
     * @return 경매 종료까지 1시간 이내인 경매 정보
     */
    List<AuctionResponse> findImminentAuctions();

    /**
     * 사용자가 낙찰한 경매 이력을 조회합니다.
     *
     * @param userId   사용자 ID
     * @param pageable 페이징 정보
     * @return 페이징된 낙찰 경매 응답 리스트
     */
    Page<WonAuctionResponse> findWonAuctionHistoryByUserId(Long userId, Pageable pageable);

    /**
     * 사용자가 낙찰하지 못한 경매 이력을 조회합니다.
     *
     * @param userId   사용자 ID
     * @param pageable 페이징 정보
     * @return 페이징된 낙찰 실패 경매 응답 리스트
     */
    Page<LostAuctionResponse> findLostAuctionHistoryByUserId(Long userId, Pageable pageable);

    /**
     * @param userId - 사용자 ID
     * @return 사용자가 참여한 상태별 경매들의 수
     */
    ParticipationCountsResponse getParticipationCounts(Long userId);

    /**
     * 주어진 사용자 ID에 해당하는 진행 중인 경매 목록을 페이징하여 조회합니다.
     *
     * @param userId   경매를 조회할 사용자 ID
     * @param pageable 페이징 정보 (페이지 번호, 페이지 크기 등)
     * @return 진행 중인 경매 목록을 포함한 페이징 결과
     */
    Page<UserAuctionResponse> findProceedingAuctionByUserId(Long userId, Pageable pageable);

    /**
     * 사용자 ID에 해당하는 종료된 경매 목록을 페이징하여 조회합니다.
     *
     * @param userId   경매를 조회할 사용자 ID
     * @param pageable 페이징 정보 (페이지 번호, 페이지 크기 등)
     * @return 종료된 경매 목록을 포함한 페이징 결과
     */
    Page<UserEndedAuctionResponse> findEndedAuctionByUserId(Long userId, Pageable pageable);

    /**
     * 낙찰 정보 조회합니다.
     */
    Optional<WonAuctionDetailsResponse> findWinningBidById(Long auctionId);
}
