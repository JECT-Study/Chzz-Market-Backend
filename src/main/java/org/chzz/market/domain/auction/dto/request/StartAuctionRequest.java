package org.chzz.market.domain.auction.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class StartAuctionRequest {
    @NotNull
    private Long productId;

    @NotNull
    private Long userId;
}
