package org.chzz.market.domain.auctionv2.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class OfficialAuctionResponse extends BaseAuctionResponse {
    private Long timeRemaining;
    private Long participantCount;
    private Boolean isParticipated;

    public OfficialAuctionResponse(Long auctionId, String productName, String imageUrl, Long minPrice, Boolean isSeller,
                                   Long timeRemaining, Long participantCount, Boolean isParticipated) {
        super(auctionId, productName, imageUrl, minPrice, isSeller);
        this.timeRemaining = timeRemaining;
        this.participantCount = participantCount;
        this.isParticipated = isParticipated;
    }
}
