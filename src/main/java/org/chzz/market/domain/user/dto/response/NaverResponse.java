package org.chzz.market.domain.user.dto.response;

import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.chzz.market.domain.user.entity.User.ProviderType;

@Slf4j
public class NaverResponse implements OAuth2Response {

    private final Map<String, Object> attribute;

    public NaverResponse(Map<String, Object> attribute) {
        this.attribute = (Map<String, Object>) attribute.get("response");
    }

    @Override
    public ProviderType getProvider() {
        return ProviderType.NAVER;
    }

    @Override
    public String getProviderId() {
        return attribute.get("id").toString();
    }

    @Override
    public String getEmail() {
        return attribute.get("email").toString();
    }

}
