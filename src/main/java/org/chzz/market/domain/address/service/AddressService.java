package org.chzz.market.domain.address.service;

import static org.chzz.market.domain.address.error.AddressErrorCode.ADDRESS_NOT_FOUND;
import static org.chzz.market.domain.user.error.UserErrorCode.USER_NOT_FOUND;

import lombok.RequiredArgsConstructor;
import org.chzz.market.domain.address.dto.request.AddressDto;
import org.chzz.market.domain.address.entity.Address;
import org.chzz.market.domain.address.error.AddressException;
import org.chzz.market.domain.address.repository.AddressRepository;
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
    public void save(Long userId, AddressDto addressDto) {
        userRepository.findById(userId)
                .ifPresentOrElse(user -> addressRepository.save(Address.toEntity(user, addressDto)), () -> {
                    throw new UserException(USER_NOT_FOUND);
                });
    }

    @Transactional
    public void updateAddress(Long userId, Long addressId, AddressDto addressDto) {
        Address address = addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new AddressException(ADDRESS_NOT_FOUND));

        // 기본 배송지로 설정한 경우, 기존의 기본 배송지를 해제
        if (addressDto.isDefault()) {
            addressRepository.updateAllDefaultToFalse(userId);
        }

        address.update(addressDto);
    }


}
