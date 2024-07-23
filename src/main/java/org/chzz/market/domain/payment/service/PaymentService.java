package org.chzz.market.domain.payment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.chzz.market.domain.payment.dto.request.ApprovalRequest;
import org.chzz.market.domain.payment.dto.response.ApprovalResponse;
import org.chzz.market.domain.payment.dto.response.TossPaymentResponse;
import org.springframework.stereotype.Service;
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentClient paymentClient;

    public ApprovalResponse approval(ApprovalRequest request) {
        TossPaymentResponse tossPaymentResponse = paymentClient.confirmPayment(request);
        return ApprovalResponse.of(tossPaymentResponse);
    }
}
