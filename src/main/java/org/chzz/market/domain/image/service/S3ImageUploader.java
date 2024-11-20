package org.chzz.market.domain.image.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
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
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "webp");

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

    public List<String> uploadImages(final Map<String, MultipartFile> multipartFiles) {
        return multipartFiles.entrySet().stream()
                .map(entry -> uploadImage(entry.getValue(), entry.getKey()))
                .toList();
    }

//    public List<String> uploadImages(List<File> images) {
//        TransferManager transfer = TransferManagerBuilder.standard().withS3Client(amazonS3Client).build();
//        MultipleFileUpload upload = transfer.uploadFileList(bucket, "", new File("."), images);
//
//        return upload.getSubTransfers().stream().map(this::getFileName).toList();
//    }
//
//    /**
//     * @return 각 파일의 key값(파일명)
//     */
//    private String getFileName(final Upload subUpload) {
//        try {
//            UploadResult uploadResult = subUpload.waitForUploadResult();
//            return uploadResult.getKey();
//        } catch (InterruptedException e) {
//            throw new ImageException(ImageErrorCode.IMAGE_UPLOAD_FAILED);
//        }
//    }
}
