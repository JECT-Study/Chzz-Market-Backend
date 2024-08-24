package org.chzz.market.domain.auction.dto.response;

public sealed interface RegisterResponse permits RegisterAuctionResponse, PreRegisterResponse {
    Long getProductId();
    String getMessage();
}
