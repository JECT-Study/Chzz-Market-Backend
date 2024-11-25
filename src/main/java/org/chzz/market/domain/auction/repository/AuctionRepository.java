package org.chzz.market.domain.auction.repository;

import java.util.Optional;
import org.chzz.market.domain.auction.entity.Auction;
import org.chzz.market.domain.auction.entity.AuctionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface AuctionRepository extends JpaRepository<Auction, Long> {
    @Query("SELECT a.status FROM Auction a WHERE a.id = :auctionId")
    Optional<AuctionStatus> findAuctionStatusById(Long auctionId);

    @Modifying
    @Query("UPDATE Auction a SET a.likeCount = a.likeCount + 1 WHERE a.id = :auctionId")
    void incrementLikeCount(Long auctionId);

    @Modifying
    @Query("UPDATE Auction a SET a.likeCount = a.likeCount - 1 WHERE a.id = :auctionId AND a.likeCount > 0")
    void decrementLikeCount(Long auctionId);

    @Modifying
    @Query("UPDATE Auction a SET a.bidCount = a.bidCount + 1 WHERE a.id = :auctionId")
    void incrementBidCount(Long auctionId);

    @Modifying
    @Query("UPDATE Auction a SET a.bidCount = a.bidCount - 1 WHERE a.id = :auctionId AND a.bidCount > 0")
    void decrementBidCount(Long auctionId);

    long countBySellerIdAndStatusIn(Long userId, AuctionStatus... status);
}
