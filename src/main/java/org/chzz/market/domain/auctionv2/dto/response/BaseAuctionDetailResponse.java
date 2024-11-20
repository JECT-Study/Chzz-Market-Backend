package org.chzz.market.domain.auctionv2.dto.response;

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.chzz.market.domain.auctionv2.entity.AuctionStatus;
import org.chzz.market.domain.auctionv2.entity.Category;
import org.chzz.market.domain.image.dto.ImageResponse;

@Getter
@NoArgsConstructor
public abstract class BaseAuctionDetailResponse {
    private Long auctionId;
    private String sellerNickname;
    private String sellerProfileImageUrl;
    private String productName;
    private String description;
    private Integer minPrice;
    protected Boolean isSeller;
    private AuctionStatus status;
    private Category category;
    private List<ImageResponse> images;

    public BaseAuctionDetailResponse(Long auctionId, String sellerNickname, String sellerProfileImageUrl,
                                     String productName, String description, Integer minPrice, Boolean isSeller,
                                     AuctionStatus status, Category category) {
        this.auctionId = auctionId;
        this.sellerNickname = sellerNickname;
        this.sellerProfileImageUrl = sellerProfileImageUrl;
        this.productName = productName;
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
