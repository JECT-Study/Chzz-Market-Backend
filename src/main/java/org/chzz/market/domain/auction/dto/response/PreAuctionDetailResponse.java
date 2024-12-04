package org.chzz.market.domain.auction.dto.response;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.chzz.market.domain.auction.entity.AuctionStatus;
import org.chzz.market.domain.auction.entity.Category;

@Getter
@NoArgsConstructor
public class PreAuctionDetailResponse extends BaseAuctionDetailResponse {
    private LocalDateTime updatedAt;
    private Long likeCount;
    private Boolean isLiked;

    public PreAuctionDetailResponse(Long auctionId, String sellerNickname, String sellerProfileImageUrl,
                                    String auctionName,
                                    String description, Integer minPrice, Boolean isSeller, AuctionStatus status,
                                    Category category, LocalDateTime updatedAt, Long likeCount, Boolean isLiked) {
        super(auctionId, sellerNickname, sellerProfileImageUrl, auctionName, description, minPrice, isSeller, status,
                category);
        this.updatedAt = updatedAt;
        this.likeCount = likeCount;
        this.isLiked = isLiked;
    }
}
