package org.chzz.market.domain.imagev2.service;

import static org.chzz.market.domain.image.error.ImageErrorCode.INVALID_IMAGE_EXTENSION;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.chzz.market.domain.auctionv2.dto.AuctionImageUpdateEvent;
import org.chzz.market.domain.auctionv2.dto.ImageUploadEvent;
import org.chzz.market.domain.auctionv2.dto.request.UpdateAuctionRequest;
import org.chzz.market.domain.auctionv2.entity.AuctionV2;
import org.chzz.market.domain.auctionv2.error.AuctionErrorCode;
import org.chzz.market.domain.auctionv2.error.AuctionException;
import org.chzz.market.domain.image.entity.ImageV2;
import org.chzz.market.domain.image.error.exception.ImageException;
import org.chzz.market.domain.image.service.S3ImageUploader;
import org.chzz.market.domain.imagev2.repository.ImageV2Repository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageV2Service {
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "webp");

    @Value("${cloud.aws.cloudfront.domain}")
    private String cloudfrontDomain;

    private final ImageV2Repository imageRepository;
    private final S3ImageUploader s3ImageUploader;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void uploadImages(final ImageUploadEvent event) {
        Map<String, MultipartFile> buffer = setImageBuffer(event);

        List<String> paths = s3ImageUploader.uploadImages(buffer);

        AuctionV2 auction = event.auction();

        List<ImageV2> list = createImages(auction, paths);

        auction.addImages(list);

        imageRepository.saveAll(list);
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void modifyImages(AuctionImageUpdateEvent event) {
        AuctionV2 auction = event.auction();
        UpdateAuctionRequest request = event.request();
        Map<String, MultipartFile> buffer = event.imageBuffer();
        Map<Long, Integer> sequence = Optional.ofNullable(request.getImageSequence()).orElse(Collections.emptyMap());
        updateAuctionImages(auction, sequence, buffer);
    }

    /**
     * key - unique한 이미지 파일명 <br/> value - 해당 파일의 {@link MultipartFile}
     */
    private Map<String, MultipartFile> setImageBuffer(final ImageUploadEvent event) {
        Map<String, MultipartFile> imageBuffer = new HashMap<>();
        for (MultipartFile image : event.images()) {
            String uniqueFileName = createUniqueFileName(image);
            imageBuffer.put(uniqueFileName, image);
        }
        return imageBuffer;
    }

    /**
     * @param paths 업로드된 이미지의 cdn 경로들
     * @return cdn과 순서를 적용한 {@link ImageV2} list
     */
    private List<ImageV2> createImages(final AuctionV2 auction, final List<String> paths) {
        return IntStream.range(0, paths.size())
                .mapToObj(i -> ImageV2.builder()
                        .cdnPath(cloudfrontDomain + "/" + paths.get(i))
                        .sequence((i + 1))
                        .auction(auction)
                        .build())
                .toList();
    }

    /**
     * @param file 업로드한 파일
     * @return 원본파일명을 기반으로한 unique한 파일명
     */
    private String createUniqueFileName(MultipartFile file) {
        String uuid = UUID.randomUUID().toString();
        String extension = StringUtils.getFilenameExtension(file.getOriginalFilename());

        if (extension == null || !isValidFileExtension(extension)) {
            throw new ImageException(INVALID_IMAGE_EXTENSION);
        }

        return uuid + "." + extension;
    }

    /**
     * 파일 확장자 검증기 <br/>
     *
     * @param extension 파일 확장자
     */
    private boolean isValidFileExtension(String extension) {
        return ALLOWED_EXTENSIONS.contains(extension.toLowerCase());
    }

    /**
     * 이미지 순서쌍을 포함한 변경요청({@link UpdateAuctionRequest})을 이용해 이미지 순서 변경
     */
    private void updateAuctionImages(final AuctionV2 auction, final Map<Long, Integer> sequence,
                                     final Map<String, MultipartFile> multipartFileBuffer) {
        validateTotalImageCount(sequence.size() + multipartFileBuffer.size());
        // 기존 이미지 처리 (업데이트할 이미지와 삭제할 이미지 구분)
        processExistingImages(auction, sequence);

        // 새 이미지가 있는 경우
        if (!multipartFileBuffer.isEmpty()) {
            uploadAndAddNewImages(auction, multipartFileBuffer);
        }
        auction.validateImageSize();// 업로드 이후 이미지 수량 검증

    }

    /**
     * 요청의 순서 변경 요청과 새로운 이미지 갯수 총합을 통해 크기 검증
     */
    private void validateTotalImageCount(int totalSize) {
        if (totalSize > 5) {
            throw new AuctionException(AuctionErrorCode.MAX_IMAGE_COUNT_EXCEEDED);
        } else if (totalSize == 0) {
            throw new AuctionException(AuctionErrorCode.INVALID_IMAGE_COUNT);
        }
    }

    /**
     * 이미지 시퀀스 수정 및 이미지 삭제
     */
    private void processExistingImages(final AuctionV2 auction, final Map<Long, Integer> sequence) {
        List<ImageV2> imagesToUpdate = new ArrayList<>();
        List<ImageV2> imagesToRemove = new ArrayList<>();

        auction.getImages().forEach(image -> {
            if (sequence.containsKey(image.getId())) {
                imagesToUpdate.add(image); // 업데이트할 이미지
            } else {
                imagesToRemove.add(image); // 삭제할 이미지
            }
        });
        auction.removeImages(imagesToRemove); // 삭제할 이미지 처리
        updateImageSequences(imagesToUpdate, sequence); // 시퀀스 업데이트할 이미지 처리
    }

    /**
     * 이미지 순서 업데이트
     */
    private void updateImageSequences(final List<ImageV2> imagesToUpdate, final Map<Long, Integer> sequence) {
        imagesToUpdate.forEach(image -> {
            Long imageId = image.getId();
            Integer newSequence = sequence.get(imageId);
            if (newSequence != null) {
                image.changeSequence(newSequence); // 이미지의 시퀀스 업데이트
            }
        });
    }

    /**
     * 이미지 업로드 및 영속화
     */
    private void uploadAndAddNewImages(final AuctionV2 auction, final Map<String, MultipartFile> multipartFileBuffer) {
        List<ImageV2> newImageEntities = uploadSequentialImages(auction, multipartFileBuffer);
        auction.addImages(newImageEntities);
        log.info("경매 ID {}번의 새 이미지를 성공적으로 저장하였습니다.", auction.getId());
    }

    private List<ImageV2> uploadSequentialImages(AuctionV2 auction, Map<String, MultipartFile> newImages) {
        List<ImageV2> images = newImages.entrySet().stream()
                .map(entry -> {
                    int sequence = Integer.parseInt(entry.getKey());
                    MultipartFile multipartFile = entry.getValue();
                    String uniqueFileName = createUniqueFileName(multipartFile);
                    String cdnPath = s3ImageUploader.uploadImage(multipartFile, uniqueFileName);
                    return ImageV2.builder()
                            .sequence(sequence)
                            .cdnPath(cloudfrontDomain + "/" + cdnPath)
                            .auction(auction)
                            .build();
                }).toList();
        imageRepository.saveAll(images);
        return images;
    }
}
