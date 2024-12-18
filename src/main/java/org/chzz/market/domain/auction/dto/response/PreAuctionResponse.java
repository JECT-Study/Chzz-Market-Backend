package org.chzz.market.domain.auction.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.chzz.market.domain.auction.dto.AuctionLikeDetail;
import org.chzz.market.domain.auction.entity.AuctionDocument;

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

    public PreAuctionResponse(AuctionDocument auctionDocument, AuctionLikeDetail dto, Long userId) {
        super(auctionDocument.getAuctionId(), auctionDocument.getName(), auctionDocument.getImageUrl(),
                Long.valueOf(auctionDocument.getMinPrice()),
                userId != null ? auctionDocument.getSellerId().equals(userId) : false);
        this.likeCount = dto.likeCount();
        this.isLiked = dto.isLiked();
    }
}
