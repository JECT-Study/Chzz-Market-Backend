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
    AUCTION_NOT_ENDED(BAD_REQUEST, "해당 경매가 아직 끝나지 않았습니다."),
    AUCTION_ALREADY_OFFICIAL(BAD_REQUEST, "해당 경매는 이미 정식 경매입니다."),
    AUCTION_ENDED(BAD_REQUEST, "해당 경매가 진행 중이 아니거나 이미 종료되었습니다."),
    OFFICIAL_AUCTION_DELETE_FORBIDDEN(FORBIDDEN, "정식경매는 삭제할수 없습니다."),
    NOW_WINNER(FORBIDDEN, "낙찰자가 아닙니다."),
    AUCTION_ACCESS_FORBIDDEN(FORBIDDEN, "해당 경매에 접근할 수 없습니다."),
    AUCTION_NOT_FOUND(NOT_FOUND, "경매를 찾을 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String message;

    public static class Const {
        public static final String AUCTION_NOT_ENDED = "AUCTION_NOT_ENDED";
        public static final String AUCTION_ALREADY_OFFICIAL = "AUCTION_ALREADY_OFFICIAL";
        public static final String AUCTION_ENDED = "AUCTION_ENDED";
        public static final String OFFICIAL_AUCTION_DELETE_FORBIDDEN = "OFFICIAL_AUCTION_DELETE_FORBIDDEN";
        public static final String NOW_WINNER = "NOW_WINNER";
        public static final String AUCTION_ACCESS_FORBIDDEN = "AUCTION_ACCESS_FORBIDDEN";
        public static final String AUCTION_NOT_FOUND = "AUCTION_NOT_FOUND";
    }
}
