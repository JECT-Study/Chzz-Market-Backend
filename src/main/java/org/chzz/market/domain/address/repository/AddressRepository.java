package org.chzz.market.domain.address.repository;

import org.chzz.market.domain.address.entity.Address;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AddressRepository extends JpaRepository<Address, Long> {

    Page<Address> findByUserId(Long userId, Pageable pageable);

    @Modifying
    @Query("UPDATE Address a SET a.isDefault = false WHERE a.user.id = :userId AND a.isDefault = true")
    void updateDefaultToFalse(@Param("userId") Long userId);

    boolean existsByUserId(Long userId);
}