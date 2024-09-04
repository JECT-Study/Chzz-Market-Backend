package org.chzz.market.domain.like.dto;

public record LikeResponse (boolean isLiked, int likeCount) {
    public static LikeResponse of(boolean isLiked, int likeCount) {
        return new LikeResponse(isLiked, likeCount);
    }
}
