package org.chzz.market.domain.auctionv2.service;

import lombok.RequiredArgsConstructor;
import org.chzz.market.domain.auctionv2.dto.response.PreAuctionResponse;
import org.chzz.market.domain.auctionv2.repository.AuctionV2QueryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AuctionMyService {
    private final AuctionV2QueryRepository auctionQueryRepository;

    /**
     * 사용자가 등록한 사전 경매 목록 조회
     */
    public Page<PreAuctionResponse> getUserPreAuctionList(Long userId, Pageable pageable) {
        return auctionQueryRepository.findPreAuctionsByUserId(userId, pageable);
    }

    /**
     * 사용자가 좋아요한 사전 경매 목록 조회
     */
    public Page<PreAuctionResponse> getLikedAuctionList(Long userId, Pageable pageable) {
        return auctionQueryRepository.findLikedAuctionsByUserId(userId, pageable);
    }

}
