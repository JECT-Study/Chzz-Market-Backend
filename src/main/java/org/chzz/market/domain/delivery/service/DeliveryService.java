package org.chzz.market.domain.delivery.service;

import static org.chzz.market.domain.delivery.error.DeliveryErrorCode.ADDRESS_NOT_FOUND;
import static org.chzz.market.domain.delivery.error.DeliveryErrorCode.CANNOT_DELETE_DEFAULT_ADDRESS;
import static org.chzz.market.domain.delivery.error.DeliveryErrorCode.FORBIDDEN_ADDRESS_ACCESS;
import static org.chzz.market.domain.user.error.UserErrorCode.USER_NOT_FOUND;

import lombok.RequiredArgsConstructor;
import org.chzz.market.domain.delivery.dto.DeliveryRequest;
import org.chzz.market.domain.delivery.dto.DeliveryResponse;
import org.chzz.market.domain.delivery.entity.Delivery;
import org.chzz.market.domain.delivery.error.DeliveryException;
import org.chzz.market.domain.delivery.repository.DeliveryRepository;
import org.chzz.market.domain.user.entity.User;
import org.chzz.market.domain.user.error.exception.UserException;
import org.chzz.market.domain.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class DeliveryService {
    private final DeliveryRepository deliveryRepository;
    private final UserRepository userRepository;


    public Page<DeliveryResponse> getAddresses(Long userId, Pageable pageable) {
        return deliveryRepository.findByUserIdOrderByIsDefaultDescCreatedAtDesc(userId, pageable)
                .map(DeliveryResponse::fromEntity);
    }

    @Transactional
    public void addDelivery(Long userId, DeliveryRequest deliveryRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(USER_NOT_FOUND));

        if (deliveryRequest.isDefault()) {
            deliveryRepository.findByUserIdAndIsDefaultTrue(userId)
                    .ifPresent(Delivery::unmarkAsDefault);
        }

        Delivery delivery = DeliveryRequest.toEntity(user, deliveryRequest);
        deliveryRepository.save(delivery);
    }

    @Transactional
    public void updateDelivery(Long userId, Long addressId, DeliveryRequest deliveryRequest) {
        Delivery delivery = deliveryRepository.findById(addressId)
                .orElseThrow(() -> new DeliveryException(ADDRESS_NOT_FOUND));

        if (!delivery.isOwner(userId)) {
            throw new DeliveryException(FORBIDDEN_ADDRESS_ACCESS);
        }

        if (deliveryRequest.isDefault() && !delivery.isDefault()) {
            deliveryRepository.findByUserIdAndIsDefaultTrue(userId)
                    .ifPresent(Delivery::unmarkAsDefault);
        }

        delivery.update(deliveryRequest);
    }

    @Transactional
    public void deleteDelivery(Long userId, Long addressId) {
        Delivery delivery = deliveryRepository.findById(addressId)
                .orElseThrow(() -> new DeliveryException(ADDRESS_NOT_FOUND));

        if (!delivery.isOwner(userId)) {
            throw new DeliveryException(FORBIDDEN_ADDRESS_ACCESS);
        }

        // 기본 배송지인 경우 삭제 불가
        if (delivery.isDefault()) {
            throw new DeliveryException(CANNOT_DELETE_DEFAULT_ADDRESS);
        }

        deliveryRepository.delete(delivery);
    }

}
