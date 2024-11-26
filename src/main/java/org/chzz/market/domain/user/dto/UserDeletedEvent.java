package org.chzz.market.domain.user.dto;

import org.chzz.market.domain.user.entity.User.ProviderType;

public record UserDeletedEvent(ProviderType type, String providerId) {
}
