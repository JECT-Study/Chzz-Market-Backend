package org.chzz.market.domain.oauth2.service;

import lombok.extern.slf4j.Slf4j;
import org.chzz.market.domain.user.dto.CustomUserDetails;
import org.chzz.market.domain.oauth2.repository.Oauth2RefreshTokenRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.authentication.OAuth2LoginAuthenticationProvider;
import org.springframework.security.oauth2.client.authentication.OAuth2LoginAuthenticationToken;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CustomOAuth2LoginAuthenticationProvider extends OAuth2LoginAuthenticationProvider {
    private final Oauth2RefreshTokenRepository oauth2RefreshTokenRepository;

    public CustomOAuth2LoginAuthenticationProvider(
            OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> accessTokenResponseClient,
            OAuth2UserService<OAuth2UserRequest, OAuth2User> userService,
            Oauth2RefreshTokenRepository oauth2RefreshTokenRepository) {
        super(accessTokenResponseClient, userService);
        this.oauth2RefreshTokenRepository = oauth2RefreshTokenRepository;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        OAuth2LoginAuthenticationToken authenticationResult = (OAuth2LoginAuthenticationToken) super.authenticate(
                authentication);
        CustomUserDetails userDetails = (CustomUserDetails) authenticationResult.getPrincipal();
        String refreshToken = authenticationResult.getRefreshToken().getTokenValue();
        String providerType = authenticationResult.getClientRegistration().getRegistrationId();
        String providerId = userDetails.getProviderId();
        oauth2RefreshTokenRepository.save(providerType, providerId, refreshToken);
        return authenticationResult;
    }
}
