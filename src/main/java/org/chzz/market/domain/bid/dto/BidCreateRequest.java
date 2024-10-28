package org.chzz.market.domain.bid.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.chzz.market.common.validation.annotation.ThousandMultiple;
import org.chzz.market.domain.auction.entity.Auction;
import org.chzz.market.domain.bid.entity.Bid;
import org.chzz.market.domain.user.entity.User;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class BidCreateRequest {
    @NotNull
    private Long auctionId;

    @ThousandMultiple(message = "1,000원 단위로 입력해주세요.")
    @Max(value = 2_000_000, message = "입찰금액은 200만원을 넘을 수 없습니다")
    private Long bidAmount;

    public Bid toEntity(Auction auction, User user) {
        return Bid.builder()
                .auction(auction)
                .bidder(user)
                .amount(bidAmount)
                .build();
    }
}
