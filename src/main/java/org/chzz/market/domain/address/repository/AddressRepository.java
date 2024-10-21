package org.chzz.market.domain.address.repository;

import java.util.Optional;
import org.chzz.market.domain.address.entity.Address;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AddressRepository extends JpaRepository<Address, Long> {

    Page<Address> findByUserIdOrderByIsDefaultDescCreatedAtDesc(Long userId, Pageable pageable);

    Optional<Address> findByUserIdAndIsDefaultTrue(Long userId);
}