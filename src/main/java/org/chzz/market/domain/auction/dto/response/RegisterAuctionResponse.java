package org.chzz.market.domain.auction.dto.response;

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
    private static final String AUCTION_SUCCESS_MESSAGE = "상품이 성공적으로 경매 등록되었습니다.";
    private static final String PRE_REGISTER_SUCCESS_MESSAGE = "상품이 성공적으로 사전 등록되었습니다.";

    public static RegisterAuctionResponse of(Long productId, Long auctionId, AuctionStatus status) {
        String message = (status != null) ? AUCTION_SUCCESS_MESSAGE : PRE_REGISTER_SUCCESS_MESSAGE;
        return new RegisterAuctionResponse(productId, auctionId, status, message);
    }
}
