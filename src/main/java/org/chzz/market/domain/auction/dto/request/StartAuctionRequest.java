package org.chzz.market.domain.auction.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class StartAuctionRequest {
    @NotNull
    private Long productId;

    @NotNull
    @Min(value = 1000, message = "경매 시작 가격은 최소 1,000원 이상, 1000의 배수이어야 합니다")
    private Integer minPrice;

    @NotNull
    @Future
    private LocalDateTime endDateTime;
}
