package org.chzz.market.domain.address.repository;

import org.chzz.market.domain.address.dto.request.AddressDto;
import org.chzz.market.domain.address.entity.Address;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AddressRepository extends JpaRepository<Address, Long> {
    @Query("SELECT new org.chzz.market.domain.address.dto.request.AddressDto(a.roadAddress, a.jibun, a.zipcode, a.detailAddress) " +
            "FROM Address a WHERE a.user.id = :userId")
    Page<AddressDto> findAddressesByUserId(Pageable pageable, @Param("userId") Long userId);
}