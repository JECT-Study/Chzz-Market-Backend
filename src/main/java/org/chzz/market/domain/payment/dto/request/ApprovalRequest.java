package org.chzz.market.domain.payment.dto.request;

import org.chzz.market.domain.payment.dto.DeliveryRequest;

public record ApprovalRequest(String orderId,
                              String paymentKey,
                              Long amount,
                              Long auctionId,
                              DeliveryRequest deliveryRequest) {
}
