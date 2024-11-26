package org.chzz.market.domain.oauth2.service;

import lombok.RequiredArgsConstructor;
import org.chzz.market.domain.user.entity.User.ProviderType;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SocialLoginServiceFactory {
    private final KakaoSocialLoginService kakaoSocialLoginService;
    private final NaverSocialLoginService naverSocialLoginService;

    public SocialLoginService getService(ProviderType providerType) {
        return switch (providerType) {
            case KAKAO -> kakaoSocialLoginService;
            case NAVER -> naverSocialLoginService;
        };
    }
}
