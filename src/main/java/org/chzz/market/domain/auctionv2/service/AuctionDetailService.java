package org.chzz.market.domain.auctionv2.service;

import static org.chzz.market.domain.auctionv2.error.AuctionErrorCode.AUCTION_NOT_FOUND;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.chzz.market.domain.auctionv2.dto.response.BaseAuctionDetailResponse;
import org.chzz.market.domain.auctionv2.entity.AuctionStatus;
import org.chzz.market.domain.auctionv2.error.AuctionException;
import org.chzz.market.domain.auctionv2.repository.AuctionV2QueryRepository;
import org.chzz.market.domain.auctionv2.repository.AuctionV2Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AuctionDetailService {
    private final AuctionV2Repository auctionRepository;
    private final AuctionV2QueryRepository auctionQueryRepository;

    public BaseAuctionDetailResponse getAuctionDetails(Long userId, Long auctionId) {
        return auctionRepository.findAuctionStatusById(auctionId)
                .flatMap(status -> getAuctionDetailByStatus(status, userId, auctionId))
                .orElseThrow(() -> new AuctionException(AUCTION_NOT_FOUND));
    }

    private Optional<BaseAuctionDetailResponse> getAuctionDetailByStatus(AuctionStatus status, Long userId,
                                                                         Long auctionId) {
        return switch (status) {
            case PRE -> auctionQueryRepository.findPreAuctionDetailById(userId, auctionId)
                    .map(response -> response);
            case PROCEEDING, ENDED -> auctionQueryRepository.findOfficialAuctionDetailById(userId, auctionId)
                    .map(response -> response.clearOrderIfNotEligible());
        };
    }
}
