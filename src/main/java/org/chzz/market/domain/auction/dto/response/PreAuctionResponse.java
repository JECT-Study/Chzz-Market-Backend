package org.chzz.market.domain.auction.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PreAuctionResponse extends BaseAuctionResponse {
    private Long likeCount;
    private Boolean isLiked;

    public PreAuctionResponse(Long auctionId, String auctionName, String imageUrl, Long minPrice, Boolean isSeller,
                              Long likeCount, Boolean isLiked) {
        super(auctionId, auctionName, imageUrl, minPrice, isSeller);
        this.likeCount = likeCount;
        this.isLiked = isLiked;
    }
}
