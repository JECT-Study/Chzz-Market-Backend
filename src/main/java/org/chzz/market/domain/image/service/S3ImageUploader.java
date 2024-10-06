package org.chzz.market.domain.image.service;

import static org.chzz.market.domain.image.error.ImageErrorCode.IMAGE_MOVE_FAILED;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.CopyObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.chzz.market.domain.image.error.ImageErrorCode;
import org.chzz.market.domain.image.error.exception.ImageException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3ImageUploader implements ImageUploader {
    private final AmazonS3 amazonS3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Override
    public String uploadImage(MultipartFile image, String fileName) {
        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(image.getSize());
            metadata.setContentType(image.getContentType());

            amazonS3Client.putObject(bucket, fileName, image.getInputStream(), metadata);

            return fileName; // CDN 경로 생성 (전체 URL 아닌 경로만)
        } catch (IOException e) {
            throw new ImageException(ImageErrorCode.IMAGE_UPLOAD_FAILED);
        }
    }

    public void moveToTemp(String fileName) {
        try {
            // 원본 객체 /temp 파일로 복사
            CopyObjectRequest copyObjectRequest = new CopyObjectRequest(
                    bucket, fileName, bucket, "temp/" + fileName
            );
            amazonS3Client.copyObject(copyObjectRequest);

            // 원본 객체 삭제
            amazonS3Client.deleteObject(bucket, fileName);

            log.info("이미지 파일을 temp 로 이동합니다. fileName: {}", fileName);
        } catch (AmazonS3Exception e) {
            throw new ImageException(IMAGE_MOVE_FAILED);
        }
    }
}
