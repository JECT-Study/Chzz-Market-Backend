package org.chzz.market.domain.auction.dto.response;

import lombok.Getter;

import static org.chzz.market.domain.auction.entity.Auction.*;

/**
 * 경매 등록 DTO
 */
public record RegisterAuctionResponse(Long productId, Long auctionId, AuctionStatus status, String message) implements RegisterResponse {
    private static final String AUCTION_SUCCESS_MESSAGE = "상품이 성공적으로 경매 등록되었습니다.";

    public static RegisterAuctionResponse of(Long productId, Long auctionId, AuctionStatus status) {
        return new RegisterAuctionResponse(productId, auctionId, status, AUCTION_SUCCESS_MESSAGE);
    }

    @Override
    public Long getProductId() {
        return productId;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
