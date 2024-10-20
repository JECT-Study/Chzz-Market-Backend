package org.chzz.market.domain.address.repository;

import java.util.Optional;
import org.chzz.market.domain.address.entity.Address;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AddressRepository extends JpaRepository<Address, Long> {

    Page<Address> findByUserId(Long userId, Pageable pageable);

    @Query("SELECT a FROM Address a WHERE a.user.id = :userId ORDER BY CASE WHEN a.isDefault = true THEN 1 ELSE 0 END DESC, a.createdAt DESC")
    Page<Address> findByUserIdOrderByIsDefaultAndCreatedAt(@Param("userId") Long userId, Pageable pageable);


    Optional<Address> findByUserIdAndIsDefaultTrue(Long userId);
}