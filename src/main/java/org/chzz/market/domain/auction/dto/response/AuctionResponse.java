package org.chzz.market.domain.auction.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;
import org.chzz.market.domain.auction.dto.BaseAuctionDto;

/**
 * 진행중인 경매 목록 조회 DTO
 */
@Getter
public class AuctionResponse extends BaseAuctionDto {
    private final Long auctionId;
    @JsonInclude(Include.NON_NULL)
    private Boolean isParticipated;

    @QueryProjection
    public AuctionResponse(Long auctionId, String name, String cdnPath, Long timeRemaining, Long minPrice,
                           Long participantCount, Boolean isParticipated) {
        super(name, cdnPath, timeRemaining, minPrice, participantCount);
        this.auctionId = auctionId;
        this.isParticipated = isParticipated;
    }

    @QueryProjection
    public AuctionResponse(Long auctionId, String name, String cdnPath, Long timeRemaining, Long minPrice,
                           Long participantCount) {
        super(name, cdnPath, timeRemaining, minPrice, participantCount);
        this.auctionId = auctionId;
    }
}
