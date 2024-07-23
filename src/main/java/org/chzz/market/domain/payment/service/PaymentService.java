package org.chzz.market.domain.payment.service;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.chzz.market.domain.payment.dto.request.ApprovalRequest;
import org.chzz.market.domain.payment.dto.response.ApprovalResponse;
import org.chzz.market.domain.payment.dto.response.TossPaymentResponse;
import org.chzz.market.domain.payment.error.PaymentErrorCode;
import org.chzz.market.domain.payment.error.PaymentException;
import org.chzz.market.domain.payment.repository.PaymentRepository;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentClient paymentClient;
    private final PaymentRepository paymentRepository;

    public ApprovalResponse approval(ApprovalRequest request) {
        validateOrderId(request.orderId());
        TossPaymentResponse tossPaymentResponse = paymentClient.confirmPayment(request);
        return ApprovalResponse.of(tossPaymentResponse);
    }

    protected void validateOrderId(String orderId) {
        if (!(!paymentRepository.existsByOrderId(orderId)
                &&paymentClient.isValidOrderId(orderId))) {
            throw new PaymentException(PaymentErrorCode.ALREADY_EXIST);
        }
    }

    /**
     * @apiNote unique한 orderId가 아닌 경우 {@link PaymentException} 발생
     * <br> 5번 재생성 후에도 unique하지 않은 경우 예외 발생 후 로직 종료
     */
    @Retryable(
            retryFor = PaymentException.class,
            recover = "throwException",
            maxAttempts = 5,
            backoff = @Backoff(delay = 1000)
    )
    public String createOrderId() {
        String orderId = UUID.randomUUID().toString();
        validateOrderId(orderId);
        return orderId;
    }

    @Recover
    private void throwException(){
        throw new PaymentException(PaymentErrorCode.CREATION_FAILURE);
    }
}
