package org.chzz.market.domain.auction.dto;

import org.chzz.market.domain.auction.entity.Auction.AuctionStatus;

import java.time.LocalDateTime;

/**
 * 경매 시작 (사전 등록 -> 경매 등록 전환) DTO
 */
public record StartResponse (
        Long auctionId,
        Long productId,
        AuctionStatus status,
        LocalDateTime endTime,
        String message
) {
    private static final String DEFAULT_SUCCESS_MESSAGE = "경매가 성공적으로 시작되었습니다.";

    public static StartResponse of(Long auctionId, Long productId, AuctionStatus status, LocalDateTime endTime) {
        return new StartResponse(auctionId, productId, status, endTime, DEFAULT_SUCCESS_MESSAGE);
    }
}
