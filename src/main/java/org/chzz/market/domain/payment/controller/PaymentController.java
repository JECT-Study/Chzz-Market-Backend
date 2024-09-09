package org.chzz.market.domain.payment.controller;

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
@RequestMapping("/payments")
public class PaymentController {
    private final PaymentService paymentService;

    @PostMapping("/approval")
    public ResponseEntity<?> approvePayment(@LoginUser Long userId, @RequestBody ApprovalRequest request) {
        ApprovalResponse approval = paymentService.approval(userId, request);
        return ResponseEntity.status(HttpStatus.OK).body(approval); // TODO: redirect to payment page
    }

    @PostMapping("/order-id")
    public ResponseEntity<?> createOrderId() { // TODO: 낙찰자에 한해서 호출 가능 API로 변경
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentService.createOrderId());
    }
}
