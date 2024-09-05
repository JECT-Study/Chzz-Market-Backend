package org.chzz.market.domain.bid.error;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.chzz.market.common.error.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum BidErrorCode implements ErrorCode {
    BID_NOT_FOUND(NOT_FOUND, "해당 입찰을 찾을 수 없습니다."),
    BID_BELOW_MIN_PRICE(BAD_REQUEST, "입찰 금액이 최소 금액보다 낮습니다."),
    BID_LIMIT_EXCEEDED(BAD_REQUEST, "입찰 횟수 제한을 초과하여 더 이상 입찰할 수 없습니다."),
    BID_SAME_AS_PREVIOUS(BAD_REQUEST, "이전 입찰금액과 동일한 금액으로 입찰할 수 없습니다."),
    BID_ALREADY_CANCELLED(BAD_REQUEST, "해당 입찰은 이미 취소되었습니다."),
    BID_BY_OWNER(FORBIDDEN, "경매 등록자는 입찰할 수 없습니다."),
    BID_NOT_ACCESSIBLE(FORBIDDEN, "해당 입찰에 접근할 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
