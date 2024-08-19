package org.chzz.market.domain.auction.dto;

import lombok.Getter;

@Getter
public enum AuctionType {
    PRE_REGISTER("사전 등록"),
    REGISTER("경매 등록");

    private final String description;

    AuctionType(String description) {
        this.description = description;
    }

}
