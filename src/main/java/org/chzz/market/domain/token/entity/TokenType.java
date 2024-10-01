package org.chzz.market.domain.token.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TokenType {
//    ACCESS(600), // 10분
    ACCESS(1800), // TODO: 임시 30분
    REFRESH(86400), // 24시간
    TEMP(1800); // 30분

    private final int expirationTime;

}
