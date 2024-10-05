package org.chzz.market.domain.auction.dto.response;

import com.querydsl.core.annotations.QueryProjection;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import org.chzz.market.domain.auction.type.AuctionStatus;
import org.chzz.market.domain.product.entity.Product.Category;

@Getter
public class AuctionDetailsResponse {
    private final Long productId;
    private final String sellerNickname;
    private final String sellerProfileImageUrl;
    private final String productName;
    private final String description;
    private final Integer minPrice;
    private final Category category;
    private final Long timeRemaining;
    private final AuctionStatus status;
    private final Boolean isSeller;
    private final Long participantCount;
    private final Boolean isParticipated;
    private final Long bidId;
    private final Long bidAmount;
    private final int remainingBidCount;
    private List<String> imageUrls = new ArrayList<>();

    @QueryProjection
    public AuctionDetailsResponse(Long productId, String sellerNickname, String sellerProfileImageUrl,
                                  String productName, String description,
                                  Integer minPrice, Category category, Long timeRemaining, AuctionStatus status,
                                  Boolean isSeller,
                                  Long participantCount, Boolean isParticipated, Long bidId, Long bidAmount,
                                  int remainingBidCount) {
        this.productId = productId;
        this.sellerNickname = sellerNickname;
        this.sellerProfileImageUrl = sellerProfileImageUrl;
        this.productName = productName;
        this.description = description;
        this.minPrice = minPrice;
        this.category = category;
        this.timeRemaining = timeRemaining;
        this.status = status;
        this.isSeller = isSeller;
        this.participantCount = participantCount;
        this.isParticipated = isParticipated;
        this.bidId = bidId;
        this.bidAmount = bidAmount;
        this.remainingBidCount = remainingBidCount;
    }

    public void addImageList(List<String> imageList) {
        this.imageUrls = imageList;
    }
}
