package org.chzz.market.domain.user.error;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.chzz.market.common.error.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum UserErrorCode implements ErrorCode {
    NICKNAME_DUPLICATION(HttpStatus.BAD_REQUEST, "닉네임이 중복되었습니다."),
    USER_NOT_MATCHED(HttpStatus.BAD_REQUEST, "사용자 정보가 일치하지 않습니다."),
    USER_ALREADY_REGISTERED(HttpStatus.BAD_REQUEST, "이미 가입된 사용자입니다."),
    UNAUTHORIZED_USER(HttpStatus.UNAUTHORIZED, "권한이 없는 사용자입니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String message;

    public static class Const {
        public static final String NICKNAME_DUPLICATION = "NICKNAME_DUPLICATION";
        public static final String USER_NOT_MATCHED = "USER_NOT_MATCHED";
        public static final String USER_ALREADY_REGISTERED = "USER_ALREADY_REGISTERED";
        public static final String UNAUTHORIZED_USER = "UNAUTHORIZED_USER";
        public static final String USER_NOT_FOUND = "USER_NOT_FOUND";
    }
}
