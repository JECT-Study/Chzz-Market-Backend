package org.chzz.market.domain.auctionv2.dto.response;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.chzz.market.domain.auctionv2.entity.AuctionStatus;
import org.chzz.market.domain.auctionv2.entity.Category;

@Getter
@NoArgsConstructor
public class PreAuctionDetailResponse extends BaseAuctionDetailResponse {
    private LocalDateTime updatedAt;
    private Long likeCount;
    private Boolean isLiked;

    public PreAuctionDetailResponse(Long auctionId, String sellerNickname, String sellerProfileImageUrl,
                                    String productName,
                                    String description, Integer minPrice, Boolean isSeller, AuctionStatus status,
                                    Category category, LocalDateTime updatedAt, Long likeCount, Boolean isLiked) {
        super(auctionId, sellerNickname, sellerProfileImageUrl, productName, description, minPrice, isSeller, status,
                category);
        this.updatedAt = updatedAt;
        this.likeCount = likeCount;
        this.isLiked = isLiked;
    }
}
