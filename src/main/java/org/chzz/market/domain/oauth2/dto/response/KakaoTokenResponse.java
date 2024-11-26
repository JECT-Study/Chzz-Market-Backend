package org.chzz.market.domain.oauth2.dto.response;

public record KakaoTokenResponse(String token_type, String access_token, Integer expires_in) {
}
