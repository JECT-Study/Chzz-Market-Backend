package org.chzz.market.domain.bid.entity;

import static org.chzz.market.domain.bid.error.BidErrorCode.BID_ALREADY_CANCELLED;
import static org.chzz.market.domain.bid.error.BidErrorCode.BID_LIMIT_EXCEEDED;
import static org.chzz.market.domain.bid.error.BidErrorCode.BID_SAME_AS_PREVIOUS;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
import org.hibernate.annotations.DynamicUpdate;

/**
 * bid 생성시 해당 경매에 입찰 기록을 통해 entity를 가져와 dirty checking 가능하도록 구현 예정
 */
@Entity
@Getter
@Table
@Builder
@AllArgsConstructor
@DynamicUpdate
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

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(255)")
    @Builder.Default
    private BidStatus status = BidStatus.ACTIVE;

    public void adjustBidAmount(Long amount) {
        validateActiveStatus();
        if (this.count <= 0) {
            throw new BidException(BID_LIMIT_EXCEEDED);
        }
        if (this.amount.equals(amount)) {
            throw new BidException(BID_SAME_AS_PREVIOUS);
        }
        this.amount = amount;
        this.count--;
    }

    public void specifyAuction(Auction auction) {
        this.auction = auction;
    }

    public void cancelBid() {
        validateActiveStatus();
        this.status = BidStatus.CANCELLED;
    }

    public boolean isOwner(Long userId) {
        return this.bidder.getId().equals(userId);
    }

    private void validateActiveStatus() {
        if (!this.status.equals(BidStatus.ACTIVE)) {
            throw new BidException(BID_ALREADY_CANCELLED);
        }
    }

    public enum BidStatus {
        ACTIVE,
        CANCELLED
    }
}
