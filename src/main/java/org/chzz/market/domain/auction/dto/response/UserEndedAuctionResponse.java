package org.chzz.market.domain.auction.dto.response;

import com.querydsl.core.annotations.QueryProjection;
import java.time.LocalDateTime;

/**
 * 사용자의 종료된 경매 목록 조회
 */
public record UserEndedAuctionResponse(
        Long auctionId,
        String productName,
        String imageUrl,
        Long minPrice,
        Long participantCount,
        Long winningBidAmount,
        Boolean isWon, // 낙찰 유무
        Boolean isOrdered, // 주문 유무
        LocalDateTime createAt
) {

    @QueryProjection
    public UserEndedAuctionResponse {
    }
}
