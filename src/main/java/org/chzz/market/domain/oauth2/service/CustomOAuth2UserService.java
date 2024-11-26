package org.chzz.market.domain.oauth2.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.chzz.market.domain.oauth2.dto.response.KaKaoResponse;
import org.chzz.market.domain.oauth2.dto.response.NaverResponse;
import org.chzz.market.domain.oauth2.dto.response.OAuth2Response;
import org.chzz.market.domain.user.dto.CustomUserDetails;
import org.chzz.market.domain.user.entity.User;
import org.chzz.market.domain.user.entity.User.ProviderType;
import org.chzz.market.domain.user.repository.UserRepository;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Transactional
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        ProviderType providerType = ProviderType.valueOf(registrationId.toUpperCase());

        if (registrationId == null) {
            throw new OAuth2AuthenticationException("유효하지않는 OAuth2 제공자입니다.");
        }
        OAuth2Response oAuth2Response = switch (providerType) {
            case NAVER -> new NaverResponse(oAuth2User.getAttributes());
            case KAKAO -> new KaKaoResponse(oAuth2User.getAttributes());
        };
        User user = findOrCreateMember(oAuth2Response, providerType);
        return new CustomUserDetails(user, oAuth2User.getAttributes());
    }

    private User findOrCreateMember(OAuth2Response oAuth2Response, ProviderType providerType) {
        return userRepository.findByProviderTypeAndProviderId(providerType, oAuth2Response.getProviderId())
                .orElseGet(() -> registerNewMember(oAuth2Response));
    }

    private User registerNewMember(OAuth2Response oAuth2Response) {
        log.info("소셜로그인으로 처음 로그인(강제 회원가입): {}", oAuth2Response.getProvider());
        User user = oAuth2Response.toEntity();
        return userRepository.save(user);
    }
}
