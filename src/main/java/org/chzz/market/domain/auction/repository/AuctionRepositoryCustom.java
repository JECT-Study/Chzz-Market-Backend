package org.chzz.market.domain.auction.repository;

import java.util.List;
import java.util.Optional;
import org.chzz.market.domain.auction.dto.response.AuctionDetailsResponse;
import org.chzz.market.domain.auction.dto.response.AuctionResponse;
import org.chzz.market.domain.auction.dto.response.UserAuctionResponse;
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
     * 사용자가 참여한(입찰한) 경매 상세 정보를 조회합니다.
     * @param userId - 사용자 ID
     * @param pageable - 페이징 정보
     * @return
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
     * 사용자 닉네임에 따라 경매 리스트를 조회합니다.
     *
     * @param nickname 사용자 닉네임
     * @param pageable 페이징 정보
     * @return 페이징된 사용자 경매 응답 리스트
     */
    Page<UserAuctionResponse> findAuctionsByNickname(String nickname, Pageable pageable);


    /**
     * 홈 화면의 베스트 경매 조회
     * @return 입찰 기록이 많은 10개의 경매 정보
     */
    List<AuctionResponse> findBestAuctions(Long userId);

    /**
     * 사용자 ID에 따라 경매 관련 counting 값들을 조회합니다.
     * @param userId 사용자 ID
     * @return 사용자의 경매 관련 counting 값들
     */
    ParticipationCountsResponse getParticipationCounts(Long userId);
}
