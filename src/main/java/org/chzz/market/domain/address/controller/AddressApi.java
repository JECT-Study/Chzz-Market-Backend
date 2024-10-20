package org.chzz.market.domain.address.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.chzz.market.domain.address.dto.request.AddressDto;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

@Tag(name = "addresses", description = "배송지 API")
public interface AddressApi {

    @Operation(summary = "배송지 목록 조회")
    ResponseEntity<Page<AddressDto>> getAddresses(Long userId, @ParameterObject Pageable pageable);

    @Operation(summary = "배송지 추가")
    ResponseEntity<Void> addAddress(Long userId, AddressDto addressDto);
}
