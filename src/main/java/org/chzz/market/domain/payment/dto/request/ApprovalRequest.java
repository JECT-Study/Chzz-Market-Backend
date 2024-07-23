package org.chzz.market.domain.payment.dto.request;

public record ApprovalRequest(String paymentKey,
                              String orderId,
                              Long amount) {
}
