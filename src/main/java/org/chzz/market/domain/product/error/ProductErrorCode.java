package org.chzz.market.domain.product.error;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.chzz.market.common.error.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ProductErrorCode implements ErrorCode {
    PRODUCT_REGISTER_FAILED(HttpStatus.BAD_REQUEST, "상품 등록에 실패했습니다."),
    INVALID_PRODUCT_STATE(HttpStatus.BAD_REQUEST, "상품 상태가 유효하지 않습니다."),
    ALREADY_IN_AUCTION(HttpStatus.BAD_REQUEST, "이미 정식경매로 등록된 상품입니다."),
    PRODUCT_ALREADY_AUCTIONED(HttpStatus.BAD_REQUEST, "상품이 이미 경매로 등록되어 삭제할 수 없습니다."),
    FORBIDDEN_PRODUCT_ACCESS(HttpStatus.FORBIDDEN, "상품에 접근할 수 없습니다."),
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "상품을 찾을 수 없습니다."),
    IMAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "상품 이미지를 찾을 수 없습니다."),
    PRODUCT_NOT_FOUND_OR_IN_AUCTION(HttpStatus.NOT_FOUND, "상품을 찾을 수 없거나 경매 상태입니다.");

    private final HttpStatus httpStatus;
    private final String message;

    public static class Const {
        public static final String PRODUCT_REGISTER_FAILED = "PRODUCT_REGISTER_FAILED";
        public static final String INVALID_PRODUCT_STATE = "INVALID_PRODUCT_STATE";
        public static final String ALREADY_IN_AUCTION = "ALREADY_IN_AUCTION";
        public static final String PRODUCT_ALREADY_AUCTIONED = "PRODUCT_ALREADY_AUCTIONED";
        public static final String FORBIDDEN_PRODUCT_ACCESS = "FORBIDDEN_PRODUCT_ACCESS";
        public static final String PRODUCT_NOT_FOUND = "PRODUCT_NOT_FOUND";
        public static final String IMAGE_NOT_FOUND = "IMAGE_NOT_FOUND";
        public static final String PRODUCT_NOT_FOUND_OR_IN_AUCTION = "PRODUCT_NOT_FOUND_OR_IN_AUCTION";
    }
}
