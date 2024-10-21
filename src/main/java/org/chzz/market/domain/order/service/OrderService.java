package org.chzz.market.domain.order.service;

import lombok.RequiredArgsConstructor;
import org.chzz.market.domain.address.entity.Address;
import org.chzz.market.domain.address.exception.AddressErrorCode;
import org.chzz.market.domain.address.exception.AddressException;
import org.chzz.market.domain.address.repository.AddressRepository;
import org.chzz.market.domain.order.entity.Order;
import org.chzz.market.domain.order.repository.OrderRepository;
import org.chzz.market.domain.payment.dto.ShippingAddressRequest;
import org.chzz.market.domain.payment.dto.SuccessfulPaymentEvent;
import org.chzz.market.domain.payment.entity.Payment;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionalEventListener;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final AddressRepository addressRepository;

    @Async("threadPoolTaskExecutor")
    @TransactionalEventListener
    public void completedTransactionProcess(SuccessfulPaymentEvent event) {
        Long userId = event.userId();
        Payment payment = event.payment();
        ShippingAddressRequest shippingAddressRequest = event.shippingAddressRequest();
        Address address = addressRepository.findById(shippingAddressRequest.addressId())
                .orElseThrow(() -> new AddressException(AddressErrorCode.NOT_FOUND));
        Order order = Order.of(userId, payment, address, shippingAddressRequest.memo());
        orderRepository.save(order);
    }
}
