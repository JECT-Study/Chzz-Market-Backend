package org.chzz.market.domain.auction.dto.response;

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.chzz.market.domain.auction.entity.AuctionStatus;
import org.chzz.market.domain.auction.entity.Category;
import org.chzz.market.domain.image.dto.response.ImageResponse;

@Getter
@NoArgsConstructor
public abstract class BaseAuctionDetailResponse {
    private Long auctionId;
    private String sellerNickname;
    private String sellerProfileImageUrl;
    private String auctionName;
    private String description;
    private Integer minPrice;
    protected Boolean isSeller;
    private AuctionStatus status;
    private Category category;
    private List<ImageResponse> images;

    public BaseAuctionDetailResponse(Long auctionId, String sellerNickname, String sellerProfileImageUrl,
                                     String auctionName, String description, Integer minPrice, Boolean isSeller,
                                     AuctionStatus status, Category category) {
        this.auctionId = auctionId;
        this.sellerNickname = sellerNickname;
        this.sellerProfileImageUrl = sellerProfileImageUrl;
        this.auctionName = auctionName;
        this.description = description;
        this.minPrice = minPrice;
        this.isSeller = isSeller;
        this.status = status;
        this.category = category;
    }

    public void addImageList(List<ImageResponse> images) {
        this.images = images;
    }
}
