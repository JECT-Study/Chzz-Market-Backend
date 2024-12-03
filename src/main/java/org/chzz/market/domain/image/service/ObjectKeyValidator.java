package org.chzz.market.domain.image.service;

import com.amazonaws.services.s3.AmazonS3;
import lombok.RequiredArgsConstructor;
import org.chzz.market.domain.image.error.ImageErrorCode;
import org.chzz.market.domain.image.error.exception.ImageException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ObjectKeyValidator {
    private final AmazonS3 amazonS3;
    private final String bucket;

    public void validate(String objectKey) {
        if (!amazonS3.doesObjectExist(bucket, objectKey)) {
            throw new ImageException(ImageErrorCode.INVALID_OBJECT_KEY);
        }
    }
}
