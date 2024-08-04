package org.chzz.market.domain.bid.error;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.chzz.market.common.error.ErrorCode;
import org.springframework.http.HttpStatus;
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum BidErrorCode implements ErrorCode {
    NOT_ENOUGH_COUNT(HttpStatus.INTERNAL_SERVER_ERROR,"입찰 가능 횟수가 모자랍니다" );
    private final HttpStatus httpStatus;
    private final String message;
}
