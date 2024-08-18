package org.chzz.market.domain.payment.error;

import org.chzz.market.common.error.BusinessException;

public class TossPaymentException extends BusinessException {
    public TossPaymentException(TossPaymentErrorCode errorCode) {
        super(errorCode);
    }
}
