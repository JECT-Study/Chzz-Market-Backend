package org.chzz.market.domain.auctionv2.repository;

import org.chzz.market.domain.auction.repository.AuctionRepositoryCustom;
import org.chzz.market.domain.auctionv2.entity.AuctionV2;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuctionV2Repository extends JpaRepository<AuctionV2, Long>, AuctionRepositoryCustom {
}
