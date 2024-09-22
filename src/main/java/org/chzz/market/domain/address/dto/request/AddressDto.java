package org.chzz.market.domain.address.dto.request;

public record AddressDto(
        String roadAddress,
        String jibun,
        String zipcode,
        String detailAddress) {
}