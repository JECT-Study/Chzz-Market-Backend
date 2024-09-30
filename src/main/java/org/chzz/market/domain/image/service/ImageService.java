package org.chzz.market.domain.image.service;

import static org.chzz.market.domain.image.error.ImageErrorCode.IMAGE_DELETE_FAILED;
import static org.chzz.market.domain.image.error.ImageErrorCode.INVALID_IMAGE_EXTENSION;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import java.util.List;
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
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageService {
    // 지원하는 이미지 확장자
    private static final List<String> ALLOWED_EXTENSIONS = List.of("jpg", "jpeg", "png", "gif", "webp");

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

        uploadedUrls.forEach(url -> log.info("업로드 된 이미지 : {}", cloudfrontDomain + url));

        return uploadedUrls;
    }

    /**
     * 단일 이미지 파일 업로드 및 CDN 경로 리스트 반환
     */
    private String uploadImage(MultipartFile image) {
        String uniqueFileName = createUniqueFileName(image.getOriginalFilename());
        return imageUploader.uploadImage(image, uniqueFileName);
    }

    /**
     * 상품에 대한 이미지 Entity 생성 및 저장
     */
    @Transactional
    public List<Image> saveProductImageEntities(Product product, List<String> cdnPaths) {
        List<Image> images = cdnPaths.stream()
                .map(cdnPath -> Image.builder()
                        .cdnPath(cloudfrontDomain + cdnPath)
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
            String key = cdnPath.substring(1);
            amazonS3Client.deleteObject(bucket, key);
        } catch (AmazonServiceException e) {
            throw new ImageException(IMAGE_DELETE_FAILED);
        }
    }

    /**
     * CDN 경로로부터 전체 이미지 URL 재구성 이미지 -> 서버에 들어왔는지 확인하는 로그에 사용
     */
    public String getFullImageUrl(String cdnPath) {
        // URL 인코딩
        return cloudfrontDomain + cdnPath;
    }

    /**
     * 고유한 파일 이름 생성 및 확장자 검증
     */
    private String createUniqueFileName(String originalFileName) {
        String uuid = UUID.randomUUID().toString();
        String extension = getFileExtension(originalFileName);

        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new ImageException(INVALID_IMAGE_EXTENSION);
        }

        return uuid + "." + extension;
    }

    /**
     * 파일 확장자 추출
     */
    private String getFileExtension(String originalFileName) {
        return originalFileName.substring(originalFileName.lastIndexOf(".") + 1);
    }
}

