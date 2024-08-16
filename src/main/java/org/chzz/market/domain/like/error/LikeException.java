package org.chzz.market.domain.like.error;

import org.chzz.market.common.error.ErrorCode;
import org.chzz.market.common.error.exception.BusinessException;

public class LikeException extends BusinessException {
    public LikeException(final ErrorCode errorCode) {
        super(errorCode);
    }
}
