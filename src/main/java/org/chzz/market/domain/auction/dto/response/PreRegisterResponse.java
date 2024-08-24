package org.chzz.market.domain.auction.dto.response;

import lombok.Getter;

/**
 * 사전 등록 DTO
 */
public record PreRegisterResponse(Long productId, String message) implements RegisterResponse {
    private static final String PRE_REGISTER_SUCCESS_MESSAGE = "상품이 성공적으로 사전 등록되었습니다.";

    public static PreRegisterResponse of(Long productId) {
        return new PreRegisterResponse(productId, PRE_REGISTER_SUCCESS_MESSAGE);
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
