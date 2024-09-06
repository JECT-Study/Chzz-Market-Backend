package org.chzz.market.domain.notification.error;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.chzz.market.common.error.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum NotificationErrorCode implements ErrorCode {
    ALREADY_READ_NOTIFICATION(BAD_REQUEST, "이미 읽은 알림입니다."),
    DELETED_NOTIFICATION(BAD_REQUEST, "삭제된 알림입니다."),
    UNAUTHORIZED_ACCESS(FORBIDDEN, "알림에 접근할 권한이 없습니다."),
    NOTIFICATION_NOT_FOUND(NOT_FOUND, "알림을 찾을 수 없습니다."),
    REDIS_MESSAGE_SEND_FAILURE(INTERNAL_SERVER_ERROR, "Redis 메시지 발신에 실패했습니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
