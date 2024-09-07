package org.chzz.market.domain.user.error;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.chzz.market.common.error.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum UserErrorCode implements ErrorCode {
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
    NICKNAME_DUPLICATION(HttpStatus.BAD_REQUEST, "닉네임이 중복되었습니다."),
    USER_NOT_MATCHED(HttpStatus.BAD_REQUEST, "사용자 정보가 일치하지 않습니다."),
    UNAUTHORIZED_USER(HttpStatus.UNAUTHORIZED, "권한이 없는 사용자입니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
