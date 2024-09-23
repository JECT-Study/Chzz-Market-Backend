package org.chzz.market.domain.product.repository;

import io.lettuce.core.dynamic.annotation.Param;
import org.chzz.market.domain.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long>, ProductRepositoryCustom {

    /*
     * 상품 아이디로 경매가 존재하는지 확인
     */
    @Query("SELECT p FROM Product p " +
            "LEFT JOIN Auction a ON a.product = p " +
            "WHERE p.id = :id AND a.id IS NULL")
    Optional<Product> findPreOrder(@Param("id") Long id);

    /*
     * 사용자 아이디로 상품 조회
     */
    Optional<Product> findByIdAndUserId(Long id, Long userId);

    /*
     * 사용자가 등록한 사전 등록 상품 수 조회
     */
    @Query("SELECT COUNT(p) FROM Product p " +
            "LEFT JOIN Auction a ON p.id = a.product.id " +
            "WHERE p.user.id = :userId AND a IS NULL")
    long countPreRegisteredProductsByUserId(@Param("userId") Long userId);
}
