package org.chzz.market.domain.oauth2.dto.response;

import static org.chzz.market.domain.user.entity.User.UserRole.TEMP_USER;

import org.chzz.market.domain.user.entity.User;
import org.chzz.market.domain.user.entity.User.ProviderType;

public interface OAuth2Response {
    ProviderType getProvider();

    String getProviderId();

    String getEmail();

    default User toEntity() {
        return User.builder()
                .email(getEmail())
                .providerType(getProvider())
                .providerId(getProviderId())
                .userRole(TEMP_USER)
                .build();
    }
}
