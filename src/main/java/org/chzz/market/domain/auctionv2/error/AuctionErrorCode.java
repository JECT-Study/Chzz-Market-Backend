package org.chzz.market.domain.auctionv2.error;

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
    AUCTION_ALREADY_OFFICIAL(BAD_REQUEST, "해당 경매는 이미 정식 경매입니다."),
    OFFICIAL_AUCTION_DELETE_FORBIDDEN(FORBIDDEN, "정식경매는 삭제할수 없습니다."),
    AUCTION_ACCESS_FORBIDDEN(FORBIDDEN, "해당 경매에 접근할 수 없습니다."),
    AUCTION_NOT_FOUND(NOT_FOUND, "경매를 찾을 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String message;

    public static class Const {
        public static final String AUCTION_ALREADY_OFFICIAL = "AUCTION_ALREADY_OFFICIAL";
        public static final String OFFICIAL_AUCTION_DELETE_FORBIDDEN = "OFFICIAL_AUCTION_DELETE_FORBIDDEN";
        public static final String AUCTION_ACCESS_FORBIDDEN = "AUCTION_ACCESS_FORBIDDEN";
        public static final String AUCTION_NOT_FOUND = "AUCTION_NOT_FOUND";
    }
}
