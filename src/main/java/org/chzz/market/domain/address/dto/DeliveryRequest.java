package org.chzz.market.domain.address.dto;

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

    public DeliveryRequest withIdDefault(boolean isDefault) {
        return new DeliveryRequest(
                roadAddress,
                jibun,
                zipcode,
                detailAddress,
                recipientName,
                phoneNumber,
                isDefault
        );
    }
}
