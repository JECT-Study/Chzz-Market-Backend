package org.chzz.market.domain.auction.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public abstract class BaseAuctionResponse {
    private Long auctionId;
    private String auctionName;
    private String imageUrl;
    private Long minPrice;
    private Boolean isSeller;

    public BaseAuctionResponse(Long auctionId, String auctionName, String imageUrl, Long minPrice, Boolean isSeller) {
        this.auctionId = auctionId;
        this.auctionName = auctionName;
        this.imageUrl = imageUrl;
        this.minPrice = minPrice;
        this.isSeller = isSeller;
    }
}
