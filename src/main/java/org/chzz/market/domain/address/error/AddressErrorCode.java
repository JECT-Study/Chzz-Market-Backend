package org.chzz.market.domain.address.error;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.chzz.market.common.error.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum AddressErrorCode implements ErrorCode {
    ADDRESS_NOT_FOUND(HttpStatus.NOT_FOUND, "주소를 찾을 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
