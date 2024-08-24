package org.chzz.market.domain.user.dto;

import org.chzz.market.domain.user.entity.User;

public record UpdateProfileResponse (
        String nickname,
        String description,
        String region,
        String url
) {
    public static UpdateProfileResponse from(User user) {
        return new UpdateProfileResponse(
                user.getNickname(),
                user.getDescription(),
                user.getRegion(),
                user.getUrl()
        );
    }
}
