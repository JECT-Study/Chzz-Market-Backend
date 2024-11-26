package org.chzz.market.domain.user.error;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.chzz.market.common.error.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum UserErrorCode implements ErrorCode {
    CANNOT_DELETE_USER_DUE_TO_ONGOING_AUCTIONS(BAD_REQUEST, "등록한 진행 중인 경매가 있어 회원 탈퇴가 불가능합니다."),
    CANNOT_DELETE_USER_DUE_TO_ONGOING_BIDS(BAD_REQUEST, "참여 중인 입찰이 있어 회원 탈퇴가 불가능합니다."),
    NICKNAME_DUPLICATION(BAD_REQUEST, "닉네임이 중복되었습니다."),
    USER_NOT_MATCHED(BAD_REQUEST, "사용자 정보가 일치하지 않습니다."),
    USER_ALREADY_REGISTERED(BAD_REQUEST, "이미 가입된 사용자입니다."),
    UNAUTHORIZED_USER(UNAUTHORIZED, "권한이 없는 사용자입니다."),
    USER_NOT_FOUND(NOT_FOUND, "사용자를 찾을 수 없습니다."),
    KAKAO_UNLINK_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "카카오 연결 끊기에 실패했습니다."),
    NAVER_UNLINK_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "네이버 연결 끊기에 실패했습니다.");

    private final HttpStatus httpStatus;
    private final String message;

    public static class Const {
        public static final String CANNOT_DELETE_USER_DUE_TO_ONGOING_AUCTIONS = "CANNOT_DELETE_USER_DUE_TO_ONGOING_AUCTIONS";
        public static final String CANNOT_DELETE_USER_DUE_TO_ONGOING_BIDS = "CANNOT_DELETE_USER_DUE_TO_ONGOING_BIDS";
        public static final String NICKNAME_DUPLICATION = "NICKNAME_DUPLICATION";
        public static final String USER_NOT_MATCHED = "USER_NOT_MATCHED";
        public static final String USER_ALREADY_REGISTERED = "USER_ALREADY_REGISTERED";
        public static final String UNAUTHORIZED_USER = "UNAUTHORIZED_USER";
        public static final String USER_NOT_FOUND = "USER_NOT_FOUND";
        public static final String KAKAO_UNLINK_FAILED = "KAKAO_UNLINK_FAILED";
        public static final String NAVER_UNLINK_FAILED = "NAVER_UNLINK_FAILED";
    }
}
