package org.chzz.market.domain.token.error.exception;

import org.chzz.market.common.error.ErrorCode;
import org.chzz.market.common.error.exception.BusinessException;

public class TokenException extends BusinessException {
    public TokenException(final ErrorCode errorCode) {
        super(errorCode);
    }
}
