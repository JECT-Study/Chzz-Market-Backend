package org.chzz.market.common.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum GlobalErrorCode implements ErrorCode {
    INVALID_REQUEST_PARAMETER(HttpStatus.BAD_REQUEST, "Invalid request parameter"),
    UNSUPPORTED_PARAMETER_TYPE(HttpStatus.BAD_REQUEST, "Unsupported type of parameter included"),
    UNSUPPORTED_PARAMETER_NAME(HttpStatus.BAD_REQUEST, "Unsupported productName of parameter included"),
    VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "Validation failed"),
    AUTHENTICATION_REQUIRED(HttpStatus.UNAUTHORIZED, "Authentication is required"),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "Access is denied"),
    UNSUPPORTED_SORT_TYPE(HttpStatus.BAD_REQUEST,"Unsupported type of sort" ),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "Resource not exists"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error"),
    EXTERNAL_API_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "external api error. check server log.");

    private final HttpStatus httpStatus;
    private final String message;
}
