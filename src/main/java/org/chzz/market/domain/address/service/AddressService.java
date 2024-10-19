package org.chzz.market.domain.address.service;

import static org.chzz.market.domain.address.error.AddressErrorCode.ADDRESS_NOT_FOUND;
import static org.chzz.market.domain.address.error.AddressErrorCode.FORBIDDEN_ADDRESS_ACCESS;
import static org.chzz.market.domain.user.error.UserErrorCode.USER_NOT_FOUND;

import lombok.RequiredArgsConstructor;
import org.chzz.market.domain.address.dto.DeliveryRequest;
import org.chzz.market.domain.address.dto.DeliveryResponse;
import org.chzz.market.domain.address.entity.Address;
import org.chzz.market.domain.address.error.AddressException;
import org.chzz.market.domain.address.repository.AddressRepository;
import org.chzz.market.domain.user.entity.User;
import org.chzz.market.domain.user.error.exception.UserException;
import org.chzz.market.domain.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AddressService {
    private final AddressRepository addressRepository;
    private final UserRepository userRepository;


    public Page<DeliveryResponse> getAddresses(Long userId, Pageable pageable) {
        return addressRepository.findByUserId(userId,
                PageRequest.of(
                        pageable.getPageNumber(),
                        pageable.getPageSize(),
                        Sort.by(Sort.Direction.DESC, "isDefault", "createdAt")
                )
        ).map(DeliveryResponse::fromEntity);
    }

    @Transactional
    public void addDelivery(Long userId, DeliveryRequest deliveryRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(USER_NOT_FOUND));

        // 첫 배송지 추가 시, 기본 배송지로 설정
        boolean shouldBeDefault = !addressRepository.existsByUserId(userId) || deliveryRequest.isDefault();

        if (shouldBeDefault) {
            addressRepository.updateDefaultToFalse(userId);
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
            addressRepository.updateDefaultToFalse(userId);
        }

        address.update(deliveryRequest);
    }

    @Transactional
    public void deleteDelivery(Long userId, Long addressId) {
        Address address = addressRepository.findById(userId)
                .orElseThrow(() -> new AddressException(ADDRESS_NOT_FOUND));

        if (!address.isOwner(userId)) {
            throw new AddressException(FORBIDDEN_ADDRESS_ACCESS);
        }

        if (address.isDefault()) {
            long otherAddressCount = addressRepository.countByUserIdAndIdNot(userId, addressId);
            if (otherAddressCount > 0) {
                // 가장 최근 생성된 주소 새로운 기본 배송지로 설정
                Address newDefaultAddress = addressRepository.findFirstByUserIdAndIdNotOrderByCreatedAtDesc(userId,
                                addressId)
                        .orElseThrow(() -> new AddressException(ADDRESS_NOT_FOUND));
                newDefaultAddress.markAsDefault();
            }
        }

        addressRepository.delete(address);
    }

}
