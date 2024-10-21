package org.chzz.market.domain.address.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.chzz.market.common.error.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum AddressErrorCode implements ErrorCode {
    NOT_FOUND(HttpStatus.NOT_FOUND,"no such address");

    private final HttpStatus httpStatus;
    private final String message;
}
