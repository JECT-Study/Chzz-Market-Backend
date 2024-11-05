package org.chzz.market.domain.delivery.dto;

import org.chzz.market.domain.delivery.entity.Delivery;

public record DeliveryResponse(
        Long id,
        String roadAddress,
        String jibun,
        String zipcode,
        String detailAddress,
        String recipientName,
        String phoneNumber,
        Boolean isDefault
) {
    public static DeliveryResponse fromEntity(Delivery delivery) {
        return new DeliveryResponse(
                delivery.getId(),
                delivery.getRoadAddress(),
                delivery.getJibun(),
                delivery.getZipcode(),
                delivery.getDetailAddress(),
                delivery.getRecipientName(),
                delivery.getPhoneNumber(),
                delivery.isDefault()
        );
    }
}
