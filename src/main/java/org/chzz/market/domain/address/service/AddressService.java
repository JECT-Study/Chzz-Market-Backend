package org.chzz.market.domain.address.service;

import static org.chzz.market.domain.address.error.AddressErrorCode.ADDRESS_NOT_FOUND;
import static org.chzz.market.domain.address.error.AddressErrorCode.CANNOT_DELETE_DEFAULT_ADDRESS;
import static org.chzz.market.domain.address.error.AddressErrorCode.FORBIDDEN_ADDRESS_ACCESS;
import static org.chzz.market.domain.user.error.UserErrorCode.USER_NOT_FOUND;

import lombok.RequiredArgsConstructor;
import org.chzz.market.domain.address.dto.DeliveryRequest;
import org.chzz.market.domain.address.entity.Address;
import org.chzz.market.domain.address.error.AddressException;
import org.chzz.market.domain.address.repository.AddressRepository;
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
public class AddressService {
    private final AddressRepository addressRepository;
    private final UserRepository userRepository;


    public Page<?> getAddresses(Long userId, Pageable pageable) {
        return addressRepository.findAddressesByUserId(pageable, userId);
    }

    @Transactional
    public void addDelivery(Long userId, DeliveryRequest deliveryRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(USER_NOT_FOUND));

        // 첫 배송지 추가 시, 기본 배송지로 설정
        boolean shouldBeDefault = !addressRepository.existsById(userId) || deliveryRequest.isDefault();

        if (shouldBeDefault) {
            addressRepository.updateAllDefaultToFalse(userId);
            deliveryRequest = deliveryRequest.withIdDefault(true);
        }

        Address address = DeliveryRequest.toEntity(user, deliveryRequest);
        addressRepository.save(address);
    }

    @Transactional
    public void updateDelivery(Long userId, Long addressId, DeliveryRequest deliveryRequest) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new AddressException(ADDRESS_NOT_FOUND));

        if (!address.isOwner(userId)) {
            throw new AddressException(FORBIDDEN_ADDRESS_ACCESS);
        }

        // 첫 배송지 추가 시, 기본 배송지로 설정
        boolean shouldBeDefault = deliveryRequest.isDefault() && !address.isDefault();
        if (shouldBeDefault) {
            addressRepository.updateAllDefaultToFalse(userId);
        }

        address.update(deliveryRequest);
    }

    @Transactional
    public void deleteDelivery(Long userId, Long addressId) {
        Address address = addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new AddressException(ADDRESS_NOT_FOUND));

        // 기본 배송지는 삭제할 수 없음
        if (address.isDefault()) {
            throw new AddressException(CANNOT_DELETE_DEFAULT_ADDRESS);
        }

        addressRepository.delete(address);
    }

}
