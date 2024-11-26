package org.chzz.market.domain.oauth2.service;

import org.chzz.market.domain.user.entity.User.ProviderType;

public interface SocialLoginService {
    String getAccessToken(ProviderType providerType, String providerId);

    void disconnect(ProviderType providerType, String providerId);
}
