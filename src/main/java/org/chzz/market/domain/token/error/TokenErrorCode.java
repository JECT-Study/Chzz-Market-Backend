package org.chzz.market.domain.token.error;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.chzz.market.common.error.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum TokenErrorCode implements ErrorCode {
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "토큰이 만료되었습니다."),
    UNSUPPORTED_TOKEN(HttpStatus.UNAUTHORIZED, "지원되지 않는 토큰 형식입니다."),
    MALFORMED_TOKEN(HttpStatus.UNAUTHORIZED, "토큰 형식이 잘못되었습니다."),
    SIGNATURE_EXCEPTION(HttpStatus.UNAUTHORIZED, "토큰 서명 검증에 실패했습니다."),
    TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "토큰이 제공되지 않았습니다."),
    INVALID_TOKEN_TYPE(HttpStatus.UNAUTHORIZED, "토큰 유형이 일치하지 않습니다."),
    REFRESH_TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "리프레시 토큰이 유효하지 않습니다.");

    public static class Const {
        public static final String INVALID_TOKEN = "INVALID_TOKEN";
        public static final String EXPIRED_TOKEN = "EXPIRED_TOKEN";
        public static final String UNSUPPORTED_TOKEN = "UNSUPPORTED_TOKEN";
        public static final String MALFORMED_TOKEN = "MALFORMED_TOKEN";
        public static final String SIGNATURE_EXCEPTION = "SIGNATURE_EXCEPTION";
        public static final String TOKEN_NOT_FOUND = "TOKEN_NOT_FOUND";
        public static final String INVALID_TOKEN_TYPE = "INVALID_TOKEN_TYPE";
        public static final String REFRESH_TOKEN_NOT_FOUND = "REFRESH_TOKEN_NOT_FOUND";
    }

    private final HttpStatus httpStatus;
    private final String message;
}
