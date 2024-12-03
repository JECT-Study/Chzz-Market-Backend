package org.chzz.market.domain.image.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.chzz.market.domain.auction.dto.AuctionImageUpdateEvent;
import org.chzz.market.domain.auction.dto.ImageUploadEvent;
import org.chzz.market.domain.auction.entity.Auction;
import org.chzz.market.domain.auction.error.AuctionErrorCode;
import org.chzz.market.domain.auction.error.AuctionException;
import org.chzz.market.domain.image.entity.Image;
import org.chzz.market.domain.image.repository.ImageRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageService {
    @Value("${cloud.aws.cloudfront.domain}")
    private String cloudfrontDomain;

    private final ImageRepository imageRepository;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void uploadImages(final ImageUploadEvent event) {
        Auction auction = event.auction();
        List<String> objectKeys = event.objectKeys();

        List<Image> images = createImages(auction, objectKeys);

        auction.addImages(images);

        imageRepository.saveAll(images);
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void modifyImages(AuctionImageUpdateEvent event) {
        Auction auction = event.auction();
        Map<Long, Integer> sequence = event.imageSequence();
        Map<String, String> objectKeyBuffer = event.objectKeyBuffer();
        updateAuctionImages(auction, sequence, objectKeyBuffer);
    }

    /**
     * @param objectKeys 업로드된 이미지의 cdn 경로들
     * @return cdn과 순서를 적용한 {@link Image} list
     */
    private List<Image> createImages(final Auction auction, final List<String> objectKeys) {
        return IntStream.range(0, objectKeys.size())
                .mapToObj(i -> Image.builder()
                        .cdnPath(cloudfrontDomain + "/" + objectKeys.get(i))
                        .sequence((i + 1))
                        .auction(auction)
                        .build())
                .toList();
    }

    private void updateAuctionImages(final Auction auction,
                                     final Map<Long, Integer> sequence,
                                     final Map<String, String> objectKeyBuffer) {
        validateTotalImageCount(sequence.size() + objectKeyBuffer.size());

        // 기존 이미지 처리 (업데이트할 이미지와 삭제할 이미지 구분)
        processExistingImages(auction, sequence);

        // 새 이미지가 있는 경우
        if (!objectKeyBuffer.isEmpty()) {
            uploadAndAddNewImages(auction, objectKeyBuffer);
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
    private void processExistingImages(final Auction auction, final Map<Long, Integer> sequence) {
        List<Image> imagesToUpdate = new ArrayList<>();
        List<Image> imagesToRemove = new ArrayList<>();

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
    private void updateImageSequences(final List<Image> imagesToUpdate, final Map<Long, Integer> sequence) {
        imagesToUpdate.forEach(image -> {
            Long imageId = image.getId();
            Integer newSequence = sequence.get(imageId);
            if (newSequence != null) {
                image.changeSequence(newSequence); // 이미지의 시퀀스 업데이트
            }
        });
    }

    private void uploadAndAddNewImages(final Auction auction, final Map<String, String> objectKeyBuffer) {
        List<Image> newImageEntities = uploadSequentialImages(auction, objectKeyBuffer);
        auction.addImages(newImageEntities);
        log.info("경매 ID {}번의 새 이미지를 성공적으로 저장하였습니다.", auction.getId());
    }

    private List<Image> uploadSequentialImages(final Auction auction, final Map<String, String> objectKeyBuffer) {
        List<Image> images = objectKeyBuffer.entrySet().stream()
                .map(entry -> {
                    int sequence = Integer.parseInt(entry.getKey());
                    String objectKey = entry.getValue();
                    return Image.builder()
                            .sequence(sequence)
                            .cdnPath(cloudfrontDomain + "/" + objectKey)
                            .auction(auction)
                            .build();
                }).toList();
        imageRepository.saveAll(images);
        return images;
    }
}
