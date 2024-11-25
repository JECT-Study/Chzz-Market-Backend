package org.chzz.market.domain.auctionv2.dto.response;

import org.chzz.market.domain.image.entity.ImageV2;

public record ImageResponse(
        Long imageId,
        String imageUrl
) {
    public static ImageResponse from(ImageV2 imageV2) {
        return new ImageResponse(imageV2.getId(), imageV2.getCdnPath());
    }
}
