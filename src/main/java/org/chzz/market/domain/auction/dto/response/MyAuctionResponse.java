package org.chzz.market.domain.auction.dto.response;

import com.querydsl.core.annotations.QueryProjection;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.ToString;
import org.chzz.market.domain.auction.dto.BaseAuctionDTO;
import org.chzz.market.domain.auction.entity.Auction.Status;

/**
 * 나의 경매 목록 조회 DTO
 */
@Getter
@ToString
public class MyAuctionResponse extends BaseAuctionDTO {
    private Long id;
    private Status status;
    private LocalDateTime createdAt;

    @QueryProjection
    public MyAuctionResponse(Long id, String name, String cdnPath, Long timeRemaining, Long minPrice,
                             Long participantCount, Status status, LocalDateTime createdAt) {
        super(name, cdnPath, timeRemaining, minPrice, participantCount);
        this.id = id;
        this.status = status;
        this.createdAt = createdAt;
    }
}
