package org.chzz.market.domain.payment.controller;

import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.chzz.market.common.config.LoginUser;
import org.chzz.market.domain.payment.dto.request.ApprovalRequest;
import org.chzz.market.domain.payment.dto.response.ApprovalResponse;
import org.chzz.market.domain.payment.service.PaymentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/payments")
public class PaymentController implements PaymentApi {
    private final PaymentService paymentService;

    @Override
    @PostMapping("/approval")
    public ResponseEntity<ApprovalResponse> approvePayment(@LoginUser Long userId, @Valid @RequestBody ApprovalRequest request) {
        ApprovalResponse approval = paymentService.approval(userId, request);
        return ResponseEntity.status(HttpStatus.OK).body(approval); // TODO: redirect to payment page
    }

    @Override
    @PostMapping("/order-id")
    public ResponseEntity<Map<String, String>> createOrderId() { // TODO: 낙찰자에 한해서 호출 가능 API로 변경
        String orderId = paymentService.createOrderId();
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("orderId", orderId));
    }
}
