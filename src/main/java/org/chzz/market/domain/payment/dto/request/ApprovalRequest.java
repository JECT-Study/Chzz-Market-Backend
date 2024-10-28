package org.chzz.market.domain.payment.dto.request;

import jakarta.validation.constraints.Max;
import org.chzz.market.common.validation.annotation.ThousandMultiple;

public record ApprovalRequest(String orderId,
                              String paymentKey,
                              @ThousandMultiple
                              @Max(value = 2_000_000, message = "결제금액은 200만원을 넘을 수 없습니다")
                              Long amount,
                              Long auctionId,
                              ShippingAddressRequest shippingAddressRequest) {
}
