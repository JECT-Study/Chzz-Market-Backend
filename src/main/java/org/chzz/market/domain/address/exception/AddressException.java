package org.chzz.market.domain.address.exception;

import org.chzz.market.common.error.ErrorCode;
import org.chzz.market.common.error.exception.BusinessException;

public class AddressException extends BusinessException {
    public AddressException(ErrorCode errorCode) {
        super(errorCode);
    }
}
