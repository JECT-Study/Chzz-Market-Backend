package org.chzz.market.domain.address.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.chzz.market.domain.address.dto.DeliveryRequest;
import org.chzz.market.domain.address.dto.DeliveryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;

@Tag(name = "addresses", description = "배송지 API")
public interface AddressApi {

    @Operation(summary = "배송지 목록 조회", description = "기본배송지가 먼저 나오고, 최신순으로 정렬합니다.")
    public ResponseEntity<Page<DeliveryResponse>> getAddresses(Long userId, Pageable pageable);

    @Operation(summary = "배송지 추가")
    public ResponseEntity<Void> addDelivery(Long userId, @Valid DeliveryRequest deliveryRequest);

    @Operation(summary = "배송지 수정")
    public ResponseEntity<Void> updateDelivery(Long userId, Long addressId, @Valid DeliveryRequest deliveryRequest);

    @Operation(summary = "배송시 삭제", description = "기본 배송지는 삭제할 수 없습니다")
    public ResponseEntity<Void> deleteDelivery(Long userId, Long addressId);
}
