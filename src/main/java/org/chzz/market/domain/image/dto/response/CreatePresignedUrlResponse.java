package org.chzz.market.domain.image.dto.response;

import java.util.Date;

public record CreatePresignedUrlResponse(String objectKey,
                                         String uploadUrl,
                                         Date expiration) {
    public static CreatePresignedUrlResponse of(final String objectKey, final String url, final Date expiration) {
        return new CreatePresignedUrlResponse(objectKey, url, expiration);
    }
}
