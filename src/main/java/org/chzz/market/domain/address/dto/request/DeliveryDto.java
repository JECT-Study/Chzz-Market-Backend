package org.chzz.market.domain.address.dto.request;

public record DeliveryDto(
        AddressDto addressDto,
        String recipientName,
        String phoneNumber,
        String deliveryMemo,
        boolean isDefault
) {
}
