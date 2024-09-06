package org.chzz.market.domain.like.repository;

import org.chzz.market.domain.like.entity.Like;
import org.chzz.market.domain.product.entity.Product;
import org.chzz.market.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LikeRepository extends JpaRepository<Like, Long> {
    Optional<Like> findByUserAndProduct(User user, Product product);
    boolean existsByUserIdAndProductId(Long userId, Long productId);
}