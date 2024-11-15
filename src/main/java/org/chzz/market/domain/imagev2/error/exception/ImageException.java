package org.chzz.market.domain.imagev2.error.exception;

import org.chzz.market.common.error.ErrorCode;
import org.chzz.market.common.error.exception.BusinessException;

public class ImageException extends BusinessException {
    public ImageException(final ErrorCode errorCode) {
        super(errorCode);
    }
}
