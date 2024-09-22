package org.chzz.market.domain.user.dto.response;

import org.chzz.market.domain.user.entity.User;

public record UpdateProfileResponse (
        String nickname,
        String bio,
        String url
) {
    public static UpdateProfileResponse from(User user) {
        return new UpdateProfileResponse(
                user.getNickname(),
                user.getBio(),
                user.getLink()
        );
    }
}
