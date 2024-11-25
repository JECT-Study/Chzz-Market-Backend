package org.chzz.market.domain.image.dto.response;

import com.querydsl.core.annotations.QueryProjection;
import org.chzz.market.domain.image.entity.Image;

public record ImageResponse(
        Long imageId,
        String imageUrl
) {
    @QueryProjection
    public ImageResponse {
    }

    public static ImageResponse from(Image image) {
        return new ImageResponse(image.getId(), image.getCdnPath());
    }
}
