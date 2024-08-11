package org.chzz.market.domain.auction.dto;

import static org.chzz.market.domain.auction.entity.Auction.*;

/**
 * 경매 등록 / 사전 등록 DTO
 */
public record RegisterAuctionResponse(
        Long productId,
        Long auctionId,
        AuctionStatus status,
        String message
) {
    private static final String DEFAULT_SUCCESS_MESSAGE = "상품이 성공적으로 등록되었습니다.";

    public static RegisterAuctionResponse of(Long productId, Long auctionId, AuctionStatus status) {
        return new RegisterAuctionResponse(productId, auctionId, status, DEFAULT_SUCCESS_MESSAGE);
    }
}
