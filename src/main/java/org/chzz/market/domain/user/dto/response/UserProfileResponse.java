package org.chzz.market.domain.user.dto.response;

import org.chzz.market.domain.user.entity.User;

public record UserProfileResponse (
        String nickname,
        String bio,
        ParticipationCountsResponse participationCount
) {
    public static UserProfileResponse of (User user, ParticipationCountsResponse counts) {
        return new UserProfileResponse(
                user.getNickname(),
                user.getBio(),
                counts
        );
    }
}
