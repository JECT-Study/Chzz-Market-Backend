package org.chzz.market.domain.auction.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StartAuctionRequest {
    @NotNull
    private Long productId;

    @NotNull
    private Long userId;
}
