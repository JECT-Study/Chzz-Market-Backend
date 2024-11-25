package org.chzz.market.domain.auction.service;

import static org.chzz.market.domain.auction.error.AuctionErrorCode.AUCTION_NOT_FOUND;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.chzz.market.domain.auction.dto.response.BaseAuctionDetailResponse;
import org.chzz.market.domain.auction.entity.AuctionStatus;
import org.chzz.market.domain.auction.error.AuctionException;
import org.chzz.market.domain.auction.repository.AuctionQueryRepository;
import org.chzz.market.domain.auction.repository.AuctionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AuctionDetailService {
    private final AuctionRepository auctionRepository;
    private final AuctionQueryRepository auctionQueryRepository;

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
