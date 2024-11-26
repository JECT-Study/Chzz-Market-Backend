package org.chzz.market.domain.oauth2.service;

import java.net.URI;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.chzz.market.domain.oauth2.dto.response.NaverTokenResponse;
import org.chzz.market.domain.oauth2.dto.response.NaverUnlinkResponse;
import org.chzz.market.domain.oauth2.repository.Oauth2RefreshTokenRepository;
import org.chzz.market.domain.user.entity.User.ProviderType;
import org.chzz.market.domain.user.error.UserErrorCode;
import org.chzz.market.domain.user.error.exception.UserException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
@Slf4j
public class NaverSocialLoginService implements SocialLoginService {
    private static final String GRANT_TYPE_DELETE = "delete";
    private static final String GRANT_TYPE_REFRESH_TOKEN = "refresh_token";
    private static final String CLIENT_ID_PARAM = "client_id";
    private static final String CLIENT_SECRET_PARAM = "client_secret";
    private static final String ACCESS_TOKEN_PARAM = "access_token";
    private static final String REFRESH_TOKEN_PARAM = "refresh_token";
    private static final String GRANT_TYPE_PARAM = "grant_type";

    private final RestClient restClient;
    private final Oauth2RefreshTokenRepository oauth2RefreshTokenRepository;

    @Value("${spring.security.oauth2.client.provider.naver.token-uri}")
    private String naverTokenUrl;

    @Value("${spring.security.oauth2.client.registration.naver.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.naver.client-secret}")
    private String clientSecret;

    @Override
    public void disconnect(ProviderType providerType, String providerId) {
        String accessToken = getAccessToken(providerType, providerId);

        URI uri = UriComponentsBuilder.fromUriString(naverTokenUrl)
                .queryParams(createDisconnectParams(accessToken))
                .build()
                .toUri();

        NaverUnlinkResponse naverUnlinkResponse = restClient.get()
                .uri(uri)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (request, response) -> {
                    log.error("네이버 연결 끊기에 실패하였습니다. 응답 상태: {}", response.getStatusCode());
                    throw new UserException(UserErrorCode.NAVER_UNLINK_FAILED);
                })
                .body(NaverUnlinkResponse.class);
        log.info("네이버 providerId {} 의 연결이 성공적으로 끊어졌습니다. 결과: {}", providerId, naverUnlinkResponse.result());
        oauth2RefreshTokenRepository.delete(providerType.getName(), providerId);
    }

    private MultiValueMap<String, String> createDisconnectParams(String accessToken) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add(CLIENT_ID_PARAM, clientId);
        params.add(CLIENT_SECRET_PARAM, clientSecret);
        params.add(ACCESS_TOKEN_PARAM, accessToken);
        params.add(GRANT_TYPE_PARAM, GRANT_TYPE_DELETE);
        return params;
    }

    @Override
    public String getAccessToken(ProviderType providerType, String providerId) {
        String refreshToken = oauth2RefreshTokenRepository.find(providerType.getName(), providerId)
                .orElseThrow(() -> new UserException(UserErrorCode.NAVER_UNLINK_FAILED));

        URI uri = UriComponentsBuilder.fromUriString(naverTokenUrl)
                .queryParams(createRefreshTokenParams(refreshToken))
                .build()
                .toUri();

        NaverTokenResponse naverTokenResponse = restClient.get()
                .uri(uri)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (request, response) -> {
                    log.error("네이버 액세스 토큰 발급에 실패하였습니다. 응답 상태: {}", response.getStatusCode());
                    throw new UserException(UserErrorCode.NAVER_UNLINK_FAILED);
                })
                .body(NaverTokenResponse.class);
        return naverTokenResponse.access_token();
    }

    private MultiValueMap<String, String> createRefreshTokenParams(String refreshToken) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add(CLIENT_ID_PARAM, clientId);
        params.add(CLIENT_SECRET_PARAM, clientSecret);
        params.add(REFRESH_TOKEN_PARAM, refreshToken);
        params.add(GRANT_TYPE_PARAM, GRANT_TYPE_REFRESH_TOKEN);
        return params;
    }
}
