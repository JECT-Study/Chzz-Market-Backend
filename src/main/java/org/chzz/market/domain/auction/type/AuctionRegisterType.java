package org.chzz.market.domain.auction.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AuctionRegisterType {
    PRE_REGISTER("사전 등록"),
    REGISTER("경매 등록");

    private final String description;
}
