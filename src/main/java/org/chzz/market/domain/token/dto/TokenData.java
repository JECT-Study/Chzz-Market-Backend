package org.chzz.market.domain.token.dto;

import lombok.Getter;

@Getter
public class TokenData {

    private String token;
    private Long userId;

    public TokenData(final String token, final Long userId) {
        this.token = token;
        this.userId = userId;
    }
}
