package org.chzz.market.domain.auction.dto.response;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.chzz.market.domain.auction.dto.AuctionBidDetail;
import org.chzz.market.domain.auction.entity.AuctionDocument;

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

    public OfficialAuctionResponse(AuctionDocument auctionDocument, AuctionBidDetail dto, Long userId) {
        super(auctionDocument.getAuctionId(), auctionDocument.getName(), auctionDocument.getImageUrl(),
                Long.valueOf(auctionDocument.getMinPrice()),
                userId != null ? auctionDocument.getSellerId().equals(userId) : false);
        long remainingTime = ChronoUnit.SECONDS.between(LocalDateTime.now(), auctionDocument.getEndDateTime());
        this.timeRemaining = remainingTime > 0 ? remainingTime : 0;
        this.participantCount = dto.bidCount();
        this.isParticipated = dto.isParticipated();
    }
}
