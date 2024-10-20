package org.chzz.market.domain.address.dto;

import org.chzz.market.domain.address.entity.Address;

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
    public static DeliveryResponse fromEntity(Address address) {
        return new DeliveryResponse(
                address.getId(),
                address.getRoadAddress(),
                address.getJibun(),
                address.getZipcode(),
                address.getDetailAddress(),
                address.getRecipientName(),
                address.getPhoneNumber(),
                address.isDefault()
        );
    }
}
