package org.chzz.market.domain.payment.dto.request;

import org.chzz.market.domain.payment.dto.ShippingAddressRequest;

public record ApprovalRequest(String orderId,
                              String paymentKey,
                              Long amount,
                              Long auctionId,
                              ShippingAddressRequest shippingAddressRequest) {
}
