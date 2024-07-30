package org.chzz.market.domain.bid.error;

import org.chzz.market.common.error.BusinessException;
import org.chzz.market.common.error.ErrorCode;

public class BidException extends BusinessException {
    public BidException(ErrorCode errorCode) {
        super(errorCode);
    }
}
