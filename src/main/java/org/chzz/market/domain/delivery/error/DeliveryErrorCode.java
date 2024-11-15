package org.chzz.market.domain.delivery.error;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.chzz.market.common.error.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum DeliveryErrorCode implements ErrorCode {
    CANNOT_DELETE_DEFAULT_ADDRESS(HttpStatus.BAD_REQUEST, "기본 배송지는 삭제할 수 없습니다."),
    FORBIDDEN_ADDRESS_ACCESS(HttpStatus.FORBIDDEN, "해당 주소에 접근할 수 없습니다."),
    ADDRESS_NOT_FOUND(HttpStatus.NOT_FOUND, "주소를 찾을 수 없습니다.");


    private final HttpStatus httpStatus;
    private final String message;
}
