package org.chzz.market.domain.auction.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class OfficialAuctionResponse extends BaseAuctionResponse {
    private Long timeRemaining;
    private Long participantCount;
    private Boolean isParticipated;

    public OfficialAuctionResponse(Long auctionId, String auctionName, String imageUrl, Long minPrice, Boolean isSeller,
                                   Long timeRemaining, Long participantCount, Boolean isParticipated) {
        super(auctionId, auctionName, imageUrl, minPrice, isSeller);
        this.timeRemaining = timeRemaining;
        this.participantCount = participantCount;
        this.isParticipated = isParticipated;
    }
}
