package org.chzz.market.domain.notification.error;

import org.chzz.market.common.error.ErrorCode;
import org.chzz.market.common.error.exception.BusinessException;

public class NotificationException extends BusinessException {
    public NotificationException(ErrorCode errorCode) {
        super(errorCode);
    }
}
