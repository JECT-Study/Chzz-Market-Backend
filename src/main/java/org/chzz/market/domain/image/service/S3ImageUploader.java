package org.chzz.market.domain.image.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import org.chzz.market.common.error.GlobalErrorCode;
import org.chzz.market.common.error.exception.ImageUploadException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3ImageUploader implements ImageUploader {
    private final AmazonS3 amazonS3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Override
    public String uploadImage(MultipartFile image) {
        String fileName = image.getOriginalFilename();
        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(image.getSize());
            metadata.setContentType(image.getContentType());

            amazonS3Client.putObject(bucket, fileName, image.getInputStream(), metadata);

            return "/" + fileName; // CDN 경로 생성 (전체 URL 아닌 경로만)
        } catch (IOException e) {
            throw new ImageUploadException(GlobalErrorCode.IMAGE_UPLOAD_FAILED);
        }
    }
}