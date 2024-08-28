package org.chzz.market.domain.product.repository;

import org.chzz.market.domain.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findByIdAndUserId(Long id, Long userId);
}
