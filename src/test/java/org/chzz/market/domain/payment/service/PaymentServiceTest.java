package org.chzz.market.domain.payment.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.chzz.market.domain.auction.entity.Auction;
import org.chzz.market.domain.payment.dto.request.ShippingAddressRequest;
import org.chzz.market.domain.payment.dto.request.ApprovalRequest;
import org.chzz.market.domain.payment.dto.response.TossPaymentResponse;
import org.chzz.market.domain.payment.entity.Payment;
import org.chzz.market.domain.payment.entity.Status;
import org.chzz.market.domain.payment.error.PaymentException;
import org.chzz.market.domain.payment.repository.PaymentRepository;
import org.chzz.market.domain.user.entity.User;
import org.chzz.market.domain.user.repository.UserRepository;
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

    @Mock
    private UserRepository userRepository;

    @Test
    @DisplayName("완료된 결제의 경우 예외를 발생시킨다")
    void assertThatValidateOrderIdWillFilterDone() {
        TossPaymentResponse tossPaymentResponse = new TossPaymentResponse();
        tossPaymentResponse.setStatus(Status.DONE);

        ShippingAddressRequest shippingAddressRequest = new ShippingAddressRequest(1L, "memo");
        ApprovalRequest approvalRequest = new ApprovalRequest("orderid", "payment", 1000L, 1L, shippingAddressRequest);

        User user = User.builder()
                .id(1L)
                .nickname("닉네임")
                .bio("자기소개")
                .build();
        when(userRepository.findById(any()))
                .thenReturn(Optional.of(user));

        when(paymentRepository.findByPayerIdAndAuctionId(any(), any()))
                .thenReturn(List.of(Payment.of(user, tossPaymentResponse, any(Auction.class))));
        assertThrows(PaymentException.class, () -> paymentService.approval(1L, approvalRequest));
    }
}
