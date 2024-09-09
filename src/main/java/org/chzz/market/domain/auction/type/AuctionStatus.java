package org.chzz.market.domain.auction.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AuctionStatus {
    PROCEEDING("진행 중"),
    ENDED("종료");

    private final String description;
}
