package org.chzz.market.domain.auction.repository;

import org.chzz.market.domain.auction.entity.Auction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuctionRepository extends JpaRepository<Auction, Long>, AuctionRepositoryCustom {
    /*
     * 상품 아이디로 경매가 존재하는지 확인
     */
    boolean existsByProductId(Long productId);

    /*
     * 사용자가 등록한 경매 수 조회
     */
    long countByProductUserId(Long userId);
}
