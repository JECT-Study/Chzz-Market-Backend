package org.chzz.market.domain.address.service;

import lombok.RequiredArgsConstructor;
import org.chzz.market.domain.address.dto.request.AddressDto;
import org.chzz.market.domain.address.entity.Address;
import org.chzz.market.domain.address.repository.AddressRepository;
import org.chzz.market.domain.user.error.UserErrorCode;
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

    @Transactional
    public void save(Long userId, AddressDto addressDto) {
        userRepository.findById(userId)
                .ifPresentOrElse(user -> addressRepository.save(Address.toEntity(user, addressDto)), () -> {
                    throw new UserException(UserErrorCode.USER_NOT_FOUND);
                });
    }

    public Page<?> getAddresses(Long userId, Pageable pageable) {
        return addressRepository.findAddressesByUserId(pageable, userId);
    }
}
