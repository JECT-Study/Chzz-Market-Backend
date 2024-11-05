package org.chzz.market.domain.delivery.repository;

import java.util.Optional;
import org.chzz.market.domain.delivery.entity.Delivery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeliveryRepository extends JpaRepository<Delivery, Long> {

    Page<Delivery> findByUserIdOrderByIsDefaultDescCreatedAtDesc(Long userId, Pageable pageable);

    Optional<Delivery> findByUserIdAndIsDefaultTrue(Long userId);
}
