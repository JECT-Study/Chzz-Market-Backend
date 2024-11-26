package org.chzz.market.domain.oauth2.dto.response;

public record NaverTokenResponse(String access_token, String token_type, String expires_in) {
}
