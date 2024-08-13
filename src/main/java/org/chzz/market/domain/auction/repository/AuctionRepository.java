package org.chzz.market.domain.auction.repository;

import org.chzz.market.domain.auction.entity.Auction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AuctionRepository extends JpaRepository<Auction, Long>, AuctionRepositoryCustom {
    Optional<Auction> findByProductId(Long productId);
    boolean existsByProductId(Long productId);
}
