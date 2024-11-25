package org.chzz.market.domain.like.repository;

import java.util.List;
import java.util.Optional;
import org.chzz.market.domain.like.entity.Like;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LikeRepository extends JpaRepository<Like, Long> {
    List<Like> findByAuctionId(Long auctionId);

    Optional<Like> findByUserIdAndAuctionId(Long userId, Long auctionId);
}
