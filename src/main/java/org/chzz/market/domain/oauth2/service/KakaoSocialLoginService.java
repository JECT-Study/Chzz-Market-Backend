package org.chzz.market.domain.oauth2.service;

import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.chzz.market.domain.oauth2.dto.response.KakaoTokenResponse;
import org.chzz.market.domain.oauth2.dto.response.KakaoUnlinkResponse;
import org.chzz.market.domain.oauth2.repository.Oauth2RefreshTokenRepository;
import org.chzz.market.domain.user.entity.User.ProviderType;
import org.chzz.market.domain.user.error.UserErrorCode;
import org.chzz.market.domain.user.error.exception.UserException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@Service
@RequiredArgsConstructor
@Slf4j
public class KakaoSocialLoginService implements SocialLoginService {
    private static final String KAKAO_UNLINK_URL = "https://kapi.kakao.com/v1/user/unlink";
    private static final String CONTENT_TYPE = APPLICATION_FORM_URLENCODED_VALUE + ";charset=utf-8";
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_TOKEN_PREFIX = "Bearer ";

    private final RestClient restClient;
    private final Oauth2RefreshTokenRepository oauth2RefreshTokenRepository;

    @Value("${spring.security.oauth2.client.provider.kakao.token-uri}")
    private String kakaoTokenUrl;

    @Value("${oauth2.kakao.rest-api-key}")
    private String restApiKey;

    @Value("${spring.security.oauth2.client.registration.kakao.client-secret}")
    private String clientSecret;

    @Override
    public void disconnect(ProviderType providerType, String providerId) {
        String accessToken = getAccessToken(providerType, providerId);
        KakaoUnlinkResponse kakaoUnlinkResponse = restClient.post()
                .uri(KAKAO_UNLINK_URL)
                .header(AUTHORIZATION_HEADER, BEARER_TOKEN_PREFIX + accessToken)
                .contentType(MediaType.valueOf(CONTENT_TYPE))
                .retrieve()
                .onStatus(HttpStatusCode::isError, (request, response) -> {
                    log.error("카카오 연결끊기에 실패하였습니다. response status={}", response.getStatusCode());
                    throw new UserException(UserErrorCode.KAKAO_UNLINK_FAILED);
                })
                .body(KakaoUnlinkResponse.class);
        log.info("카카오 providerId {} 의 연결이 성공적으로 끊어졌습니다.", kakaoUnlinkResponse.id());
        oauth2RefreshTokenRepository.delete(providerType.getName(), providerId);
    }

    @Override
    public String getAccessToken(ProviderType providerType, String providerId) {
        String refreshToken = oauth2RefreshTokenRepository.find(providerType.getName(), providerId)
                .orElseThrow(() -> new UserException(UserErrorCode.KAKAO_UNLINK_FAILED));
        KakaoTokenResponse kakaoTokenResponse = restClient.post()
                .uri(kakaoTokenUrl)
                .contentType(MediaType.valueOf(CONTENT_TYPE))
                .body(createRefreshTokenRequestBody(refreshToken))
                .retrieve()
                .onStatus(HttpStatusCode::isError, (request, response) -> {
                    log.error("Access 토큰 발급에 실패하였습니다. 응답 상태: {}", response.getStatusCode());
                    throw new UserException(UserErrorCode.KAKAO_UNLINK_FAILED);
                })
                .body(KakaoTokenResponse.class);
        return kakaoTokenResponse.access_token();
    }

    private MultiValueMap<String, String> createRefreshTokenRequestBody(String refreshToken) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "refresh_token");
        body.add("client_id", restApiKey);
        body.add("refresh_token", refreshToken);
        body.add("client_secret", clientSecret);
        return body;
    }
}
