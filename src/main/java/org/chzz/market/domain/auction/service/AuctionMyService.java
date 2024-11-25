package org.chzz.market.domain.auction.service;

import lombok.RequiredArgsConstructor;
import org.chzz.market.domain.auction.dto.response.EndedAuctionResponse;
import org.chzz.market.domain.auction.dto.response.LostAuctionResponse;
import org.chzz.market.domain.auction.dto.response.PreAuctionResponse;
import org.chzz.market.domain.auction.dto.response.ProceedingAuctionResponse;
import org.chzz.market.domain.auction.dto.response.WonAuctionResponse;
import org.chzz.market.domain.auction.repository.AuctionQueryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AuctionMyService {
    private final AuctionQueryRepository auctionQueryRepository;

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

    /**
     * 사용자가 등록한 진행 중인 경매 목록 조회
     */
    public Page<ProceedingAuctionResponse> getUserProceedingAuctionList(Long userId, Pageable pageable) {
        return auctionQueryRepository.findProceedingAuctionsByUserId(userId, pageable);
    }

    /**
     * 사용자가 등록한 종료된 경매 목록 조회
     */
    public Page<EndedAuctionResponse> getUserEndedAuctionList(Long userId, Pageable pageable) {
        return auctionQueryRepository.findEndedAuctionsByUserId(userId, pageable);
    }

    /**
     * 사용자가 낙찰 성공한 경매 목록 조회
     */
    public Page<WonAuctionResponse> getUserWonAuctionList(Long userId, Pageable pageable) {
        return auctionQueryRepository.findWonAuctionsByUserId(userId, pageable);
    }

    /**
     * 사용자가 낙찰 실패한 경매 목록 조회
     */
    public Page<LostAuctionResponse> getUserLostAuctionList(Long userId, Pageable pageable) {
        return auctionQueryRepository.findLostAuctionsByUserId(userId, pageable);
    }
}
