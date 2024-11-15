package org.chzz.market.common.error;

import lombok.Builder;

@Builder
public record ErrorResponse(String name,
                            String[] message,
                            int status) {
    public static ErrorResponse from(final ErrorCode errorCode) {
        return ErrorResponse.builder()
                .name(errorCode.name())
                .status(errorCode.getHttpStatus().value())
                .message(new String[]{errorCode.getMessage()})
                .build();
    }

    public static ErrorResponse of(final ErrorCode errorCode, final String[] detailedErrorMessage) {
        return ErrorResponse.builder()
                .name(errorCode.name())
                .status(errorCode.getHttpStatus().value())
                .message(detailedErrorMessage)
                .build();
    }
}
