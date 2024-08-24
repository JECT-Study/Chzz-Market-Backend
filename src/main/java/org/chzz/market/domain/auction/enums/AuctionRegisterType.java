package org.chzz.market.domain.auction.enums;

import lombok.Getter;

@Getter
public enum AuctionRegisterType {
    PRE_REGISTER("사전 등록"),
    REGISTER("경매 등록");

    private final String description;

    AuctionRegisterType(String description) {
        this.description = description;
    }

}
