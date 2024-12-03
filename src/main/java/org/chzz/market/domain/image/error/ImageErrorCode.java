package org.chzz.market.domain.image.error;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.chzz.market.common.error.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ImageErrorCode implements ErrorCode {
    INVALID_IMAGE_EXTENSION(HttpStatus.BAD_REQUEST, "지원하지 않는 이미지 확장자입니다."),
    IMAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "이미지가 존재하지 않습니다."),
    IMAGE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "이미지 업로드를 실패했습니다."),
    IMAGE_DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "이미지 삭제를 실패했습니다."),
    INVALID_OBJECT_KEY(HttpStatus.INTERNAL_SERVER_ERROR,"존재하지 않는 object key입니다."),;

    private final HttpStatus httpStatus;
    private final String message;

    public static class Const {
        public static final String INVALID_IMAGE_EXTENSION = "INVALID_IMAGE_EXTENSION";
        public static final String IMAGE_NOT_FOUND = "IMAGE_NOT_FOUND";
        public static final String IMAGE_UPLOAD_FAILED = "IMAGE_UPLOAD_FAILED";
        public static final String IMAGE_DELETE_FAILED = "IMAGE_DELETE_FAILED";
        public static final String INVALID_OBJECT_KEY = "INVALID_OBJECT_KEY";
    }
}
