package org.chzz.market.domain.payment.dto;

import org.chzz.market.domain.payment.dto.request.ShippingAddressRequest;
import org.chzz.market.domain.payment.entity.Payment;

public record SuccessfulPaymentEvent(Long userId, Payment payment, ShippingAddressRequest shippingAddressRequest) {
}
