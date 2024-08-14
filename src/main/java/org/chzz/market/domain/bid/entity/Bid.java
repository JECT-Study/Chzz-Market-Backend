package org.chzz.market.domain.bid.entity;

import static org.chzz.market.domain.bid.error.BidErrorCode.BID_LIMIT_EXCEEDED;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.chzz.market.domain.auction.entity.Auction;
import org.chzz.market.domain.base.entity.BaseTimeEntity;
import org.chzz.market.domain.bid.error.BidException;
import org.chzz.market.domain.user.entity.User;
import org.hibernate.annotations.ColumnDefault;

/**
 * bid 생성시 해당 경매에 입찰 기록을 통해 entity를 가져와 dirty checking 가능하도록 구현 예정
 */
@Entity
@Getter
@Table
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Bid extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bid_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User bidder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auction_id", nullable = false)
    private Auction auction;

    @Column(nullable = false)
    private Long amount;

    @Column(nullable = false)
    @ColumnDefault(value = "3")
    @Builder.Default
    private int count = 3;

    public void adjustBidAmount(Long amount) {
        if (this.count <= 0) {
            throw new BidException(BID_LIMIT_EXCEEDED);
        }
        this.amount = amount; // TODO: 기존 입찰 가격과의 비교
        this.count--;
    }

    public void specifyAuction(Auction auction) {
        this.auction=auction;
    }
}
