package org.chzz.market.domain.auction.dto.response;

import com.querydsl.core.annotations.QueryProjection;
import java.util.List;
import lombok.Getter;
import org.chzz.market.domain.auction.type.AuctionStatus;

@Getter
public class AuctionDetailsResponse {
    private Long productId;
    private String sellerName;
    private String name;
    private String description;
    private Integer minPrice;
    private Long timeRemaining;
    private AuctionStatus status;
    private Boolean isSeller;
    private Long participantCount;
    private Boolean isParticipating;
    private Long bidId;
    private Long bidAmount;
    private int remainingBidCount;
    private List<String> imageList;

    @QueryProjection
    public AuctionDetailsResponse(Long productId, String sellerName, String name, String description,
                                  Integer minPrice, Long timeRemaining, AuctionStatus status,
                                  Boolean isSeller,
                                  Long participantCount, Boolean isParticipating, Long bidId, Long bidAmount,
                                  int remainingBidCount) {
        this.productId = productId;
        this.sellerName = sellerName;
        this.name = name;
        this.description = description;
        this.minPrice = minPrice;
        this.timeRemaining = timeRemaining;
        this.status = status;
        this.isSeller = isSeller;
        this.participantCount = participantCount;
        this.isParticipating = isParticipating;
        this.bidId = bidId;
        this.bidAmount = bidAmount;
        this.remainingBidCount = remainingBidCount;
    }

    public void addImageList(List<String> imageList) {
        this.imageList = imageList;
    }
}
