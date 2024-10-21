package org.chzz.market.domain.payment.dto;

import jakarta.annotation.Nullable;

public record DeliveryRequest(Long addressId,
                              @Nullable
                              String memo) {
}
