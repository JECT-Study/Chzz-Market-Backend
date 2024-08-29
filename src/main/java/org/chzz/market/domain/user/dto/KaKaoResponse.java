package org.chzz.market.domain.user.dto;

import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.chzz.market.domain.user.entity.User.ProviderType;

@Slf4j
public class KaKaoResponse implements OAuth2Response {

    private final Map<String, Object> attribute;

    public KaKaoResponse(Map<String, Object> attribute) {
        this.attribute = attribute;
    }

    @Override
    public ProviderType getProvider() {
        return ProviderType.KAKAO;
    }

    @Override
    public String getProviderId() {
        return attribute.get("id").toString();
    }

    @Override
    public String getEmail() {
        return ((Map<String, Object>) attribute.get("kakao_account")).get("email").toString();
    }

}
