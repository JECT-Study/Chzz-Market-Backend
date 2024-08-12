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
}
