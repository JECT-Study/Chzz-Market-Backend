package org.chzz.market.domain.payment.dto;

import org.chzz.market.domain.payment.entity.Payment;

public record SuccessfulPaymentEvent(Long userId, Payment payment, DeliveryRequest deliveryRequest) {
}
