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
    END_WITHIN_MINUTES_PARAM_ALLOWED_FOR_PROCEEDING_ONLY(BAD_REQUEST,
            "진행중인 경매 목록 조회 시에만 minutes 파라미터를 사용할 수 있습니다."),
    INVALID_IMAGE_COUNT(HttpStatus.BAD_REQUEST, "이미지 개수가 올바르지 않습니다."),
    MAX_IMAGE_COUNT_EXCEEDED(HttpStatus.BAD_REQUEST, "이미지는 최대 5개까지 등록할 수 있습니다."),
    NOT_A_PRE_AUCTION(BAD_REQUEST, "사전 등록 경매가 아닙니다"),
    NO_IMAGES_PROVIDED(HttpStatus.BAD_REQUEST, "이미지가 제공되지 않았습니다."),
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
        public static final String END_WITHIN_MINUTES_PARAM_ALLOWED_FOR_PROCEEDING_ONLY = "END_WITHIN_MINUTES_PARAM_ALLOWED_FOR_PROCEEDING_ONLY";
        public static final String INVALID_IMAGE_COUNT = "INVALID_IMAGE_COUNT";
        public static final String OFFICIAL_AUCTION_DELETE_FORBIDDEN = "OFFICIAL_AUCTION_DELETE_FORBIDDEN";
        public static final String MAX_IMAGE_COUNT_EXCEEDED = "MAX_IMAGE_COUNT_EXCEEDED";
        public static final String NOT_A_PRE_AUCTION = "NOT_A_PRE_AUCTION";
        public static final String NO_IMAGES_PROVIDED = "NO_IMAGES_PROVIDED";
        public static final String NOW_WINNER = "NOW_WINNER";
        public static final String AUCTION_ACCESS_FORBIDDEN = "AUCTION_ACCESS_FORBIDDEN";
        public static final String AUCTION_NOT_FOUND = "AUCTION_NOT_FOUND";
    }
}
