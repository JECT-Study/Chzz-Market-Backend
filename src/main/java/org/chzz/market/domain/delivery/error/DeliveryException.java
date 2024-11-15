package org.chzz.market.domain.delivery.error;

import org.chzz.market.common.error.ErrorCode;
import org.chzz.market.common.error.exception.BusinessException;

public class DeliveryException extends BusinessException {
    public DeliveryException(final ErrorCode errorCode) {
        super(errorCode);
    }
}
