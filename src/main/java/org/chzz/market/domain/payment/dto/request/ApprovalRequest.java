package org.chzz.market.domain.payment.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record ApprovalRequest(String orderId,
                              String paymentKey,
                              @Min(value = 1000, message = "결제금액은 최소 1,000원 이상, 1000의 배수이어야 합니다")
                              @Max(value = 2_000_000,message = "결제금액은 200만원을 넘을 수 없습니다")
                              Long amount,
                              Long auctionId,
                              ShippingAddressRequest shippingAddressRequest) {
}
