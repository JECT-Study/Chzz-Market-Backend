package org.chzz.market.domain.auction.dto.response;

import com.querydsl.core.annotations.QueryProjection;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.ToString;
import org.chzz.market.domain.auction.dto.BaseAuctionDTO;

import static org.chzz.market.domain.auction.entity.Auction.*;

/**
 * 나의 경매 목록 조회 DTO
 */
@Getter
@ToString
public class UserAuctionResponse extends BaseAuctionDTO {
    private Long id;
    private AuctionStatus status;
    private LocalDateTime createdAt;

    @QueryProjection
    public UserAuctionResponse(Long id, String name, String cdnPath, Long timeRemaining, Long minPrice,
                               Long participantCount, AuctionStatus status, LocalDateTime createdAt) {
        super(name, cdnPath, timeRemaining, minPrice, participantCount);
        this.id = id;
        this.status = status;
        this.createdAt = createdAt;
    }
}
