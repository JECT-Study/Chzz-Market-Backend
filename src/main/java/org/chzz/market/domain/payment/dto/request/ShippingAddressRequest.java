package org.chzz.market.domain.payment.dto.request;

import jakarta.annotation.Nullable;

public record ShippingAddressRequest(Long addressId,
                                     @Nullable
                                     String memo) {
}
