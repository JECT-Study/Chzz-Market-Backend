package org.chzz.market.domain.auctionv2.repository;

import java.util.Optional;
import org.chzz.market.domain.auction.repository.AuctionRepositoryCustom;
import org.chzz.market.domain.auctionv2.entity.AuctionStatus;
import org.chzz.market.domain.auctionv2.entity.AuctionV2;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface AuctionV2Repository extends JpaRepository<AuctionV2, Long>, AuctionRepositoryCustom {
    @Query("SELECT a.status FROM AuctionV2 a WHERE a.id = :auctionId")
    Optional<AuctionStatus> findAuctionStatusById(Long auctionId);

    @Modifying
    @Query("UPDATE AuctionV2 a SET a.likeCount = a.likeCount + 1 WHERE a.id = :auctionId")
    void incrementLikeCount(Long auctionId);

    @Modifying
    @Query("UPDATE AuctionV2 a SET a.likeCount = a.likeCount - 1 WHERE a.id = :auctionId AND a.likeCount > 0")
    void decrementLikeCount(Long auctionId);

    @Modifying
    @Query("UPDATE AuctionV2 a SET a.bidCount = a.bidCount + 1 WHERE a.id = :auctionId")
    void incrementBidCount(Long auctionId);

    @Modifying
    @Query("UPDATE AuctionV2 a SET a.bidCount = a.bidCount - 1 WHERE a.id = :auctionId AND a.bidCount > 0")
    void decrementBidCount(Long auctionId);
}
