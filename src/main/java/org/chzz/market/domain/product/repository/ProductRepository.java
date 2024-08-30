package org.chzz.market.domain.product.repository;

import io.lettuce.core.dynamic.annotation.Param;
import org.chzz.market.domain.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long>, ProductRepositoryCustom {
    @Query("SELECT p FROM Product p " +
            "LEFT JOIN Auction a ON a.product = p " +
            "WHERE p.id = :id AND a.id IS NULL")
    Optional<Product> findPreOrder(@Param("id") Long id);

    Optional<Product> findByIdAndUserId(Long id, Long userId);
}
