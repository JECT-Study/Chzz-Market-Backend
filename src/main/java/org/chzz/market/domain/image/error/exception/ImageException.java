package org.chzz.market.domain.image.error.exception;

import org.chzz.market.common.error.ErrorCode;
import org.chzz.market.common.error.exception.BusinessException;

public class ImageException extends BusinessException {
    private String detailedMessage;

    public ImageException(final ErrorCode errorCode) {
        super(errorCode);
    }

    public ImageException(ErrorCode errorCode, String detailedMessage) {
        super(errorCode);
        this.detailedMessage = detailedMessage;
    }

    public String getDetailedMessage() {
        return detailedMessage != null ? detailedMessage : super.getMessage();
    }
}
