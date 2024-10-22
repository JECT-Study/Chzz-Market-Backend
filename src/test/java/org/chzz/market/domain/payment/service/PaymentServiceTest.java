package org.chzz.market.domain.payment.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;
import org.chzz.market.domain.payment.dto.response.TossPaymentResponse;
import org.chzz.market.domain.payment.entity.Payment;
import org.chzz.market.domain.payment.entity.Status;
import org.chzz.market.domain.payment.error.PaymentException;
import org.chzz.market.domain.payment.repository.PaymentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {
    @InjectMocks
    private PaymentService paymentService;

    @Mock
    private PaymentRepository paymentRepository;


    @Test
    @DisplayName("완료된 결제의 경우 예외를 발생시킨다")
    void assertThatValidateOrderIdWillFilterDone() {
        TossPaymentResponse tossPaymentResponse = new TossPaymentResponse();
        tossPaymentResponse.setStatus(Status.DONE);
        when(paymentRepository.findByPayerIdAndAuctionId(any(),any()))
                .thenReturn(List.of(Payment.of(any(), tossPaymentResponse,any())));
        assertThrows(PaymentException.class, () -> paymentService.validateDuplicatePayment(0L, 0L));
    }
}
