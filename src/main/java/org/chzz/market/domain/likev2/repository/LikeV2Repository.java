package org.chzz.market.domain.likev2.repository;

import java.util.List;
import org.chzz.market.domain.likev2.entity.LikeV2;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LikeV2Repository extends JpaRepository<LikeV2, Long> {
    List<LikeV2> findByAuctionId(Long auctionId);
}
