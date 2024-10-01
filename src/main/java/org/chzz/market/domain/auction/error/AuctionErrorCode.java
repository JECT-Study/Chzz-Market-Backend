package org.chzz.market.domain.auction.error;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.chzz.market.common.error.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum AuctionErrorCode implements ErrorCode {
    AUCTION_ENDED(BAD_REQUEST, "경매가 종료되었습니다."),
    AUCTION_NOT_FOUND(NOT_FOUND, "경매를 찾을 수 없습니다."),
    INVALID_AUCTION_STATE(BAD_REQUEST, "경매 상태가 유효하지 않습니다."),
    AUCTION_ALREADY_REGISTERED(BAD_REQUEST, "이미 등록된 경매입니다."),
    UNKNOWN_AUCTION_TYPE(BAD_REQUEST, "알 수 없는 경매 타입입니다."),
    AUCTION_NOT_ENDED(BAD_REQUEST, "아직 경매가 종료되지 않았습니다."),
    NOT_WINNER(FORBIDDEN, "낙찰자가 아닙니다."),
    FORBIDDEN_AUCTION_ACCESS(FORBIDDEN, "해당 경매에 접근할 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
