package org.chzz.market.domain.bid.error;

import static org.springframework.http.HttpStatus.*;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.chzz.market.common.error.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum BidErrorCode implements ErrorCode {
    BID_BELOW_MIN_PRICE(BAD_REQUEST, "입찰 금액이 최소 금액보다 낮습니다."),
    BID_LIMIT_EXCEEDED(BAD_REQUEST, "입찰 횟수 제한을 초과하여 더 이상 입찰할 수 없습니다."),
    BID_BY_OWNER(FORBIDDEN, "경매 등록자는 입찰할 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
