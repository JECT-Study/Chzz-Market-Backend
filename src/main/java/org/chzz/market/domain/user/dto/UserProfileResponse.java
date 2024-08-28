package org.chzz.market.domain.user.dto;

import org.chzz.market.domain.user.entity.User;

public record UserProfileResponse (
        String nickname,
        String region,
        String bio,
        ParticipationCountsResponse participationCount
) {
    public static UserProfileResponse of (User user, ParticipationCountsResponse counts) {
        return new UserProfileResponse(
                user.getNickname(),
                user.getRegion(),
                user.getBio(),
                counts
        );
    }
}
