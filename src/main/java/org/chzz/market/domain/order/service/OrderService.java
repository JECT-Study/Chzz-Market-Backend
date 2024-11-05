package org.chzz.market.domain.order.service;

import lombok.RequiredArgsConstructor;
import org.chzz.market.domain.delivery.entity.Delivery;
import org.chzz.market.domain.delivery.error.DeliveryErrorCode;
import org.chzz.market.domain.delivery.error.DeliveryException;
import org.chzz.market.domain.delivery.repository.DeliveryRepository;
import org.chzz.market.domain.order.entity.Order;
import org.chzz.market.domain.order.repository.OrderRepository;
import org.chzz.market.domain.payment.dto.request.ShippingAddressRequest;
import org.chzz.market.domain.payment.dto.SuccessfulPaymentEvent;
import org.chzz.market.domain.payment.entity.Payment;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {
    private final OrderRepository orderRepository;
    private final DeliveryRepository deliveryRepository;

    @Async("threadPoolTaskExecutor")
    @TransactionalEventListener
    public void completedTransactionProcess(SuccessfulPaymentEvent event) {
        Long userId = event.userId();
        Payment payment = event.payment();
        ShippingAddressRequest shippingAddressRequest = event.shippingAddressRequest();
        Delivery delivery = deliveryRepository.findById(shippingAddressRequest.addressId())
                .orElseThrow(() -> new DeliveryException(DeliveryErrorCode.ADDRESS_NOT_FOUND));
        Order order = Order.of(userId, payment, delivery, shippingAddressRequest.memo());
        orderRepository.save(order);
    }
}
