package org.chzz.market.domain.auctionv2.error;

import org.chzz.market.common.error.ErrorCode;
import org.chzz.market.common.error.exception.BusinessException;

public class AuctionException extends BusinessException {
    public AuctionException(final ErrorCode errorCode) {
        super(errorCode);
    }
}
