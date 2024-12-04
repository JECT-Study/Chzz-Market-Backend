package org.chzz.market.domain.auction.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.chzz.market.domain.auction.entity.AuctionStatus;
import org.chzz.market.domain.auction.entity.Category;

@Getter
@NoArgsConstructor
public class OfficialAuctionDetailResponse extends BaseAuctionDetailResponse {
    private Long timeRemaining;
    private Long participantCount;
    private Boolean isParticipated;
    private Long bidId;
    private Long bidAmount;
    private int remainingBidCount;
    private Boolean isCancelled;
    @Schema(description = "낙찰자인지 여부")
    private Boolean isWinner;
    @Schema(description = "낙찰되었는지 여부")
    private Boolean isWon;
    @Schema(description = "주문 여부 - 판매자와 낙찰자에게만 제공")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean isOrdered;

    public OfficialAuctionDetailResponse(Long auctionId, String sellerNickname, String sellerProfileImageUrl,
                                         String auctionName, String description, Integer minPrice, Boolean isSeller,
                                         AuctionStatus status, Category category, Long timeRemaining,
                                         Long participantCount, Boolean isParticipated, Long bidId, Long bidAmount,
                                         int remainingBidCount, Boolean isCancelled, Boolean isWinner, Boolean isWon,
                                         Boolean isOrdered) {
        super(auctionId, sellerNickname, sellerProfileImageUrl, auctionName, description, minPrice, isSeller, status,
                category);
        this.timeRemaining = timeRemaining;
        this.participantCount = participantCount;
        this.isParticipated = isParticipated;
        this.bidId = bidId;
        this.bidAmount = bidAmount;
        this.remainingBidCount = remainingBidCount;
        this.isCancelled = isCancelled;
        this.isWinner = isWinner;
        this.isWon = isWon;
        this.isOrdered = isOrdered;
    }

    public OfficialAuctionDetailResponse clearOrderIfNotEligible() {
        if (!isSeller && !isWinner) {
            this.isOrdered = null;
        }
        return this;
    }
}
