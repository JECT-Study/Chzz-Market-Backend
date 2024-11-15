package org.chzz.market.domain.auction.dto.response;

import com.querydsl.core.annotations.QueryProjection;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.ToString;
import org.chzz.market.domain.auction.dto.BaseAuctionDto;

import org.chzz.market.domain.auction.type.AuctionStatus;

/**
 * 나의 경매 목록 조회 DTO
 */
@Getter
@ToString
public class UserAuctionResponse extends BaseAuctionDto {
    private final Long auctionId;
    private final AuctionStatus status;
    private final LocalDateTime createdAt;

    @QueryProjection
    public UserAuctionResponse(Long auctionId, String name, String cdnPath, Long timeRemaining, Long minPrice,
                               Long participantCount, AuctionStatus status, LocalDateTime createdAt) {
        super(name, cdnPath, timeRemaining, minPrice, participantCount);
        this.auctionId = auctionId;
        this.status = status;
        this.createdAt = createdAt;
    }
}
