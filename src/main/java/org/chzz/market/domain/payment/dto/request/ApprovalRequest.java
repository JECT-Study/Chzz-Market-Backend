package org.chzz.market.domain.payment.dto.request;
public record ApprovalRequest(String paymentKey,
                              Long amount,
                              String orderId) {
}
