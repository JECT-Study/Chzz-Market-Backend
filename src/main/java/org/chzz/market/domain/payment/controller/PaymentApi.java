package org.chzz.market.domain.payment.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import org.chzz.market.domain.payment.dto.request.ApprovalRequest;
import org.chzz.market.domain.payment.dto.response.ApprovalResponse;
import org.springframework.http.ResponseEntity;


@Tag(name = "payments", description = "결제 API")
public interface PaymentApi {

    @Operation(summary = "결제 승인")
    public ResponseEntity<ApprovalResponse> approvePayment(Long userId, ApprovalRequest request);

    @Operation(summary = "주문 ID 생성")
    public ResponseEntity<Map<String, String>> createOrderId();
}
