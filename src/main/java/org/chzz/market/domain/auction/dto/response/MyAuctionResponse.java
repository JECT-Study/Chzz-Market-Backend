package org.chzz.market.domain.auction.dto.response;

import com.querydsl.core.annotations.QueryProjection;
import java.time.Duration;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.ToString;
import org.chzz.market.domain.auction.entity.Auction.Status;

/**
 * 나의 경매 목록 조회 DTO
 */
@Getter
@ToString
public class MyAuctionResponse {
    private Long id;
    private String name;
    private String cdnPath;
    private Long timeRemaining;
    private Integer minPrice;
    private Long participantCount;
    private Status status;
    private LocalDateTime createdAt;

    @QueryProjection
    public MyAuctionResponse(Long id, String name, String cdnPath, LocalDateTime endDateTime, Integer minPrice,
                             Long participantCount, Status status, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.cdnPath = cdnPath;
        this.timeRemaining = calculateTimeRemaining(endDateTime);
        this.minPrice = minPrice;
        this.participantCount = participantCount;
        this.status = status;
        this.createdAt = createdAt;
    }

    /**
     * 경매 종료까지 남은 시간 계산
     *
     * @param endDateTime
     * @return 초 단위로 남은 시간
     */
    private Long calculateTimeRemaining(LocalDateTime endDateTime) {
        long seconds = Duration.between(LocalDateTime.now(), endDateTime).getSeconds();
        return seconds < 0 ? 0L : seconds;
    }
}
