package org.chzz.market.domain.address.dto.request;

import lombok.Builder;
import org.chzz.market.domain.address.entity.Address;
import org.chzz.market.domain.user.entity.User;

@Builder
public record DeliveryRequest(
        String roadAddress,
        String jibun,
        String zipcode,
        String detailAddress,
        String recipientName,
        String phoneNumber,
        boolean isDefault
) {
    public static Address toEntity(User user, DeliveryRequest dto) {
        return Address.builder()
                .user(user)
                .roadAddress(dto.roadAddress())
                .jibun(dto.jibun())
                .zipcode(dto.zipcode())
                .detailAddress(dto.detailAddress())
                .recipientName(dto.recipientName())
                .phoneNumber(dto.phoneNumber())
                .isDefault(dto.isDefault())
                .build();
    }

    public static DeliveryRequest fromEntity(Address address) {
        return DeliveryRequest.builder()
                .roadAddress(address.getRoadAddress())
                .jibun(address.getJibun())
                .zipcode(address.getZipcode())
                .detailAddress(address.getDetailAddress())
                .recipientName(address.getRecipientName())
                .phoneNumber(address.getPhoneNumber())
                .isDefault(address.isDefault())
                .build();
    }
}
