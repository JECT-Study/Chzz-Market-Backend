package org.chzz.market.domain.imagev2.service;

import static org.chzz.market.domain.imagev2.error.ImageErrorCode.IMAGE_DELETE_FAILED;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.chzz.market.domain.image.entity.ImageV2;
import org.chzz.market.domain.imagev2.error.exception.ImageException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@Slf4j
public class ImageDeleteService {
    private final AmazonS3 amazonS3Client;
    private final String bucket;

    public ImageDeleteService(AmazonS3 amazonS3Client, @Qualifier("s3BucketName") String bucket) {
        this.amazonS3Client = amazonS3Client;
        this.bucket = bucket;
    }

    /**
     * S3에서 이미지 삭제
     */
    public void deleteImages(List<ImageV2> images) {
        images.stream()
                .map(ImageV2::getCdnPath)
                .forEach(this::deleteImage);
    }

    /**
     * 단일 이미지 삭제
     */
    private void deleteImage(String cdnPath) {
        try {
            String key = new URL(cdnPath).getPath().substring(1);
            amazonS3Client.deleteObject(bucket, key);
            log.info("S3에서 객체 삭제, Key : {}", key);
        } catch (AmazonServiceException | MalformedURLException e) {
            throw new ImageException(IMAGE_DELETE_FAILED);
        }
    }
}

