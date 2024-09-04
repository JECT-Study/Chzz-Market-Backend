package org.chzz.market.domain.user.dto;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import org.chzz.market.domain.user.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

@Getter
public class CustomUserDetails implements OAuth2User {

    private User user;
    private Map<String, Object> attributes;

    public CustomUserDetails(User user) {
        this.user = user;
    }

    public CustomUserDetails(User user, Map<String, Object> attributes) {
        this.user = user;
        this.attributes = attributes;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of((GrantedAuthority) () -> user.getUserRole().getValue());
    }

    @Override
    public String getName() {
        return user.getEmail();
    }
}
