package org.chzz.market.domain.auctionv2.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public abstract class BaseAuctionResponse {
    private Long auctionId;
    private String productName;
    private String imageUrl;
    private Long minPrice;
    private Boolean isSeller;

    public BaseAuctionResponse(Long auctionId, String productName, String imageUrl, Long minPrice, Boolean isSeller) {
        this.auctionId = auctionId;
        this.productName = productName;
        this.imageUrl = imageUrl;
        this.minPrice = minPrice;
        this.isSeller = isSeller;
    }
}
