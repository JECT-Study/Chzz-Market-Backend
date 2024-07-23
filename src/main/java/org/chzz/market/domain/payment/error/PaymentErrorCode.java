package org.chzz.market.domain.payment.error;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.chzz.market.common.error.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum PaymentErrorCode implements ErrorCode {
    INVALID_METHOD(HttpStatus.BAD_REQUEST,"결제 수단이 옳지 않습니다.");
    private final HttpStatus httpStatus;
    private final String message;
}
