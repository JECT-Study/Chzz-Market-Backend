package org.chzz.market.domain.imagev2.dto.response;

import com.querydsl.core.annotations.QueryProjection;
import org.chzz.market.domain.image.entity.ImageV2;

public record ImageResponse(
        Long imageId,
        String imageUrl
) {
    @QueryProjection
    public ImageResponse {
    }

    public static ImageResponse from(ImageV2 imageV2) {
        return new ImageResponse(imageV2.getId(), imageV2.getCdnPath());
    }
}
