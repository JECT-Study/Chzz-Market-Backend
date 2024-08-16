package org.chzz.market.domain.product.repository;

import io.lettuce.core.dynamic.annotation.Param;
import org.chzz.market.domain.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long>, ProductRepositoryCustom {
    @Query("SELECT p FROM Product p WHERE p.id = :id AND NOT EXISTS (SELECT a FROM Auction a WHERE a.product = p)")
    Optional<Product> findProductForLike(@Param("id") Long id);
}
