package org.chzz.market.domain.auction.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.chzz.market.domain.auction.dto.AuctionImageUpdateEvent;
import org.chzz.market.domain.auction.dto.request.UpdateAuctionRequest;
import org.chzz.market.domain.auction.dto.response.UpdateAuctionResponse;
import org.chzz.market.domain.auction.entity.Auction;
import org.chzz.market.domain.auction.error.AuctionErrorCode;
import org.chzz.market.domain.auction.error.AuctionException;
import org.chzz.market.domain.auction.repository.AuctionRepository;
import org.chzz.market.domain.image.service.ObjectKeyValidator;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuctionModifyService {
    private final AuctionRepository auctionRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final ObjectKeyValidator objectKeyValidator;

    @Transactional
    public UpdateAuctionResponse updateAuction(Long userId, Long auctionId,
                                               UpdateAuctionRequest request) {
        // 경매 조회
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new AuctionException(AuctionErrorCode.AUCTION_NOT_FOUND));

        // 사용자 권한 체크
        auction.validateOwner(userId);

        // 경매 등록 상태 유무 유효성 검사
        if (!auction.isPreAuction()) {
            throw new AuctionException(AuctionErrorCode.NOT_A_PRE_AUCTION);
        }

        // 경매 정보 업데이트
        auction.update(request);

        request.getObjectKeyBuffer().values().forEach(objectKeyValidator::validate);

        // 이미지 업데이트 이벤트
        AuctionImageUpdateEvent event = new AuctionImageUpdateEvent(auction, request.getImageSequence(),
                request.getObjectKeyBuffer());
        eventPublisher.publishEvent(event);

        log.info("경매 ID {}번에 대한 사전 등록 정보를 업데이트를 완료했습니다.", auctionId);
        return UpdateAuctionResponse.from(auction);
    }

}
