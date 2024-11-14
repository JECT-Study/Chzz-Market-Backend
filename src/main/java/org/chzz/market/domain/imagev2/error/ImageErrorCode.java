package org.chzz.market.domain.imagev2.error;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.chzz.market.common.error.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ImageErrorCode implements ErrorCode {
    IMAGE_DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "이미지 삭제를 실패했습니다."),
    IMAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "이미지가 존재하지 않습니다.");

    private final HttpStatus httpStatus;
    private final String message;

    public static class Const {
        public static final String IMAGE_DELETE_FAILED = "IMAGE_DELETE_FAILED";
        public static final String IMAGE_NOT_FOUND = "IMAGE_NOT_FOUND";
    }
}