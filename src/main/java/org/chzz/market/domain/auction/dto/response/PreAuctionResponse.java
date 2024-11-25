package org.chzz.market.domain.auction.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PreAuctionResponse extends BaseAuctionResponse {
    private Long likeCount;
    private Boolean isLiked;

    public PreAuctionResponse(Long auctionId, String productName, String imageUrl, Long minPrice, Boolean isSeller,
                              Long likeCount, Boolean isLiked) {
        super(auctionId, productName, imageUrl, minPrice, isSeller);
        this.likeCount = likeCount;
        this.isLiked = isLiked;
    }
}
