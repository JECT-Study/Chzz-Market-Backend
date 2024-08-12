package org.chzz.market.domain.auction.dto.response;

import org.chzz.market.domain.auction.entity.Auction;

import java.time.LocalDateTime;

import static org.chzz.market.domain.auction.entity.Auction.*;

/**
 * 경매 시작 (사전 등록 -> 경매 등록 전환) DTO
 */
public record StartAuctionResponse(
        Long auctionId,
        Long productId,
        AuctionStatus status,
        LocalDateTime endTime,
        String message
) {
    private static final String DEFAULT_SUCCESS_MESSAGE = "경매가 성공적으로 시작되었습니다.";

    public static StartAuctionResponse of(Long auctionId, Long productId, AuctionStatus status, LocalDateTime endDateTime) {
        return new StartAuctionResponse(auctionId, productId, status, endDateTime, DEFAULT_SUCCESS_MESSAGE);
    }
}
