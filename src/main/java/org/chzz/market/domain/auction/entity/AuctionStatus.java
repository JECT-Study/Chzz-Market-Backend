package org.chzz.market.domain.auction.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AuctionStatus {
    PROCEEDING("진행중"),
    ENDED("종료됨"),
    PRE("사전경매");

    private final String description;
}
