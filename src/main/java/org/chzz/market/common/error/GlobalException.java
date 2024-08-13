package org.chzz.market.common.error;

import lombok.Getter;

@Getter
public class GlobalException extends RuntimeException{
    private final GlobalErrorCode errorCode;

    public GlobalException(final GlobalErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
