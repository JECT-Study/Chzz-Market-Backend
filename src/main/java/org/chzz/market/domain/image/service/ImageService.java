package org.chzz.market.domain.image.service;

import static org.chzz.market.domain.image.error.ImageErrorCode.IMAGE_DELETE_FAILED;
import static org.chzz.market.domain.image.error.ImageErrorCode.INVALID_IMAGE_EXTENSION;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.chzz.market.domain.image.entity.Image;
import org.chzz.market.domain.image.error.exception.ImageException;
import org.chzz.market.domain.image.repository.ImageRepository;
import org.chzz.market.domain.product.entity.Product;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageService {
    private final ImageUploader imageUploader;
    private final ImageRepository imageRepository;
    private final AmazonS3 amazonS3Client;

    @Value("${cloud.aws.cloudfront.domain}")
    private String cloudfrontDomain;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    /**
     * 여러 이미지 파일 업로드 및 CDN 경로 리스트 반환
     */
    public List<String> uploadImages(List<MultipartFile> images) {
        List<String> uploadedUrls = images.stream()
                .map(this::uploadImage)
                .toList();

        uploadedUrls.forEach(url -> log.info("업로드 된 이미지 : {}", cloudfrontDomain + "/" + url));

        return uploadedUrls;
    }

    /**
     * 단일 이미지 파일 업로드 및 CDN 경로 리스트 반환
     */
    private String uploadImage(MultipartFile image) {
        String uniqueFileName = createUniqueFileName(Objects.requireNonNull(image.getOriginalFilename()));

        return imageUploader.uploadImage(image, uniqueFileName);
    }

    /**
     * 상품에 대한 이미지 Entity 생성 및 저장
     */
    @Transactional
    public List<Image> saveProductImageEntities(Product product, List<String> cdnPaths) {
        List<Image> images = cdnPaths.stream()
                .map(cdnPath -> Image.builder()
                        .cdnPath(cloudfrontDomain + "/" + cdnPath)
                        .product(product)
                        .build())
                .toList();
        imageRepository.saveAll(images);

        return images;
    }

    /**
     * 업로드된 이미지 삭제
     */
    public void deleteUploadImages(List<String> fullImageUrls) {
        fullImageUrls.forEach(this::deleteImage);
    }

    /**
     * 단일 이미지 삭제
     */
    private void deleteImage(String cdnPath) {
        try {
            URL url = new URL(cdnPath);
            String path = url.getPath();
            String key = path.substring(1);

            log.info("S3에서 객체 삭제 시도, Key : {}", key);
            amazonS3Client.deleteObject(bucket, key);
        } catch (AmazonServiceException | MalformedURLException e) {
            throw new ImageException(IMAGE_DELETE_FAILED);
        }
    }

    /**
     * 고유한 파일 이름 생성
     */
    private String createUniqueFileName(String originalFileName) {
        String uuid = UUID.randomUUID().toString();
        String extension = StringUtils.getFilenameExtension(originalFileName);

        if (extension == null || !isValidFileExtension(extension)) {
            throw new ImageException(INVALID_IMAGE_EXTENSION);
        }

        return uuid + "." + extension;
    }

    /**
     * 파일 확장자 검증
     */
    private boolean isValidFileExtension(String extension) {
        List<String> allowedExtensions = Arrays.asList("jpg", "jpeg", "png", "webp");
        return allowedExtensions.contains(extension.toLowerCase());
    }
}

