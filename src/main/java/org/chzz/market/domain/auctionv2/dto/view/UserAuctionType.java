package org.chzz.market.domain.auctionv2.dto.view;

import lombok.Getter;

@Getter
public enum UserAuctionType {
    PROCEEDING, ENDED, PRE, WON, LOST, LIKED;
}
