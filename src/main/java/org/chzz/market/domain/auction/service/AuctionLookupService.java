package org.chzz.market.domain.auction.service;

import static org.chzz.market.domain.auction.error.AuctionErrorCode.END_WITHIN_MINUTES_PARAM_ALLOWED_FOR_PROCEEDING_ONLY;

import lombok.RequiredArgsConstructor;
import org.chzz.market.domain.auction.entity.AuctionStatus;
import org.chzz.market.domain.auction.entity.Category;
import org.chzz.market.domain.auction.error.AuctionException;
import org.chzz.market.domain.auction.repository.AuctionQueryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AuctionLookupService {
    private final AuctionQueryRepository auctionQueryRepository;

    /**
     * 경매 목록 조회
     */
    public Page<?> getAuctionList(Long userId, Category category, AuctionStatus status, Integer endWithinMinutes,
                                  Pageable pageable) {
        if (endWithinMinutes != null && !status.equals(AuctionStatus.PROCEEDING)) {
            throw new AuctionException(END_WITHIN_MINUTES_PARAM_ALLOWED_FOR_PROCEEDING_ONLY);
        }
        Integer endWithinSeconds = endWithinMinutes != null ? endWithinMinutes * 60 : null;
        return switch (status) {
            case PRE -> auctionQueryRepository.findPreAuctions(userId, category, pageable);
            case PROCEEDING, ENDED ->
                    auctionQueryRepository.findOfficialAuctions(userId, category, status, endWithinSeconds, pageable);
        };
    }
}
