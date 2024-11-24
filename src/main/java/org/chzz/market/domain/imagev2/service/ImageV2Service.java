package org.chzz.market.domain.imagev2.service;

import static org.chzz.market.domain.image.error.ImageErrorCode.INVALID_IMAGE_EXTENSION;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.chzz.market.domain.auctionv2.dto.ImageUploadEvent;
import org.chzz.market.domain.auctionv2.entity.AuctionV2;
import org.chzz.market.domain.image.entity.ImageV2;
import org.chzz.market.domain.image.error.exception.ImageException;
import org.chzz.market.domain.image.service.S3ImageUploader;
import org.chzz.market.domain.imagev2.repository.ImageV2Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ImageV2Service {
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "webp");

    private final ImageV2Repository imageRepository;
    private final S3ImageUploader s3ImageUploader;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void uploadImages(final ImageUploadEvent event) {
        Map<String, MultipartFile> buffer = setImageBuffer(event);

        List<String> paths = s3ImageUploader.uploadImages(buffer);

        AuctionV2 auction = event.auction();

        List<ImageV2> list = paths.stream()
                .map(path -> createImage(path, auction)).toList();

        auction.addImages(list);

        imageRepository.saveAll(list);
    }

    private Map<String, MultipartFile> setImageBuffer(final ImageUploadEvent event) {
        Map<String, MultipartFile> imageBuffer = new HashMap<>();
        for (MultipartFile image : event.images()) {
            String originalFilename = image.getOriginalFilename();
            String uniqueFileName = createUniqueFileName(originalFilename);
            imageBuffer.put(uniqueFileName, image);
        }
        return imageBuffer;
    }

    private ImageV2 createImage(final String path, final AuctionV2 auction) {
        return ImageV2.builder()
                .auction(auction)
                .cdnPath(path)
                .build();
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
