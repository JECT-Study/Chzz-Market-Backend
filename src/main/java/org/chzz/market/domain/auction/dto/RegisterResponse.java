package org.chzz.market.domain.auction.dto;

import static org.chzz.market.domain.auction.entity.Auction.*;

/**
 * 경매 등록 / 사전 등록 DTO
 */
public record RegisterResponse(Long productId, Long auctionId, AuctionStatus status, String message) {
}
