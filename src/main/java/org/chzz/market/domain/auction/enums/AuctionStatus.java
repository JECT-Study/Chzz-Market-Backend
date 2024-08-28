package org.chzz.market.domain.auction.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AuctionStatus {
    PENDING("대기 중"),
    PROCEEDING("진행 중"),
    ENDED("종료"),
    CANCELLED("취소 됨");

    private final String description;
}
