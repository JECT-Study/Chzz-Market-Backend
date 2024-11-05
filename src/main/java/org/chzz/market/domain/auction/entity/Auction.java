package org.chzz.market.domain.auction.entity;

import static org.chzz.market.domain.auction.error.AuctionErrorCode.AUCTION_ENDED;
import static org.chzz.market.domain.auction.error.AuctionErrorCode.AUCTION_NOT_ENDED;
import static org.chzz.market.domain.auction.type.AuctionStatus.PROCEEDING;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.chzz.market.domain.auction.entity.listener.AuctionEntityListener;
import org.chzz.market.domain.auction.error.AuctionException;
import org.chzz.market.domain.auction.type.AuctionStatus;
import org.chzz.market.domain.base.entity.BaseTimeEntity;
import org.chzz.market.domain.product.entity.Product;

@Getter
@Entity
@Table(indexes = {
        @Index(name = "idx_auction_end_date_time", columnList = "end_date_time")
})
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(value = AuctionEntityListener.class)
public class Auction extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "auction_id")
    private Long id;

    @OneToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @Column
    private Long winnerId;

    @Column
    private LocalDateTime endDateTime;

    @Column(columnDefinition = "varchar(20)")
    @Enumerated(EnumType.STRING)
    private AuctionStatus status;

    public Integer getMinPrice() {
        return product.getMinPrice();
    }

    public static Auction toEntity(Product product) {
        return Auction.builder()
                .product(product)
                .status(PROCEEDING)
                .endDateTime(LocalDateTime.now().plusHours(24))
                .build();
    }

    public void validateAuctionEndTime() {
        // 경매가 진행중이 아닐 때
        if (status != PROCEEDING || LocalDateTime.now().isAfter(endDateTime)) {
            throw new AuctionException(AUCTION_ENDED);
        }
    }

    // 입찰 금액이 최소 금액 이상인지 확인
    public boolean isAboveMinPrice(Long amount) {
        return amount >= getMinPrice();
    }

    public void endAuction() {
        this.status = AuctionStatus.ENDED;
    }

    public void assignWinner(Long winnerId) {
        this.winnerId = winnerId;
    }

    public void validateAuctionEnded() {
        if (!this.status.equals(AuctionStatus.ENDED)) {
            throw new AuctionException(AUCTION_NOT_ENDED);
        }
    }

    /**
     * 경매가 진행중인지 확인
     */
    public boolean isProceeding() {
        return status == PROCEEDING && LocalDateTime.now().isBefore(endDateTime);
    }

    /**
     * 낙찰자인지 확인
     */
    public boolean isWinner(Long userId) {
        return winnerId != null && winnerId.equals(userId);
    }
}
