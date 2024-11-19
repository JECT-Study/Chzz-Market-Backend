package org.chzz.market.domain.imagev2.service;

import static org.chzz.market.domain.image.error.ImageErrorCode.INVALID_IMAGE_EXTENSION;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.chzz.market.domain.auctionv2.dto.ImageUploadEvent;
import org.chzz.market.domain.auctionv2.entity.AuctionV2;
import org.chzz.market.domain.image.entity.ImageV2;
import org.chzz.market.domain.image.error.ImageErrorCode;
import org.chzz.market.domain.image.error.exception.ImageException;
import org.chzz.market.domain.image.service.S3ImageUploader;
import org.chzz.market.domain.imagev2.repository.ImageV2Repository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ImageV2Service {
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "webp");

    private final ImageV2Repository imageRepository;
    private final S3ImageUploader s3ImageUploader;

    @Async(value = "threadPoolTaskExecutor")
    @Transactional
    @TransactionalEventListener
    public void uploadImages(final ImageUploadEvent event) {
        List<File> files = event.images().stream()
                .map(this::multipartFileToFile).toList();

        List<String> paths = s3ImageUploader.uploadImages(files);

        AuctionV2 auction = event.auction();

        List<ImageV2> list = paths.stream()
                .map(path -> createImage(path, auction)).toList();

        imageRepository.saveAll(list);
    }

    private ImageV2 createImage(final String path, final AuctionV2 auction) {
        return ImageV2.builder()
                .auction(auction)
                .cdnPath(path)
                .build();
    }

    private File multipartFileToFile(MultipartFile multipartFile) {
        File file;
        try {
            file = new File(createUniqueFileName(multipartFile.getOriginalFilename()));
            multipartFile.transferTo(file);
        } catch (IOException | IllegalStateException e) {
            throw new ImageException(ImageErrorCode.IMAGE_CONVERSION_FAILURE);
        }
        return file;
    }

    private String createUniqueFileName(String originalFileName) {
        String uuid = UUID.randomUUID().toString();
        String extension = StringUtils.getFilenameExtension(originalFileName);

        if (extension == null || !isValidFileExtension(extension)) {
            throw new ImageException(INVALID_IMAGE_EXTENSION);
        }

        return uuid + "." + extension;
    }

    private boolean isValidFileExtension(String extension) {
        return ALLOWED_EXTENSIONS.contains(extension.toLowerCase());
    }
}
