package org.chzz.market.domain.auctionv2.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.chzz.market.domain.auction.entity.listener.AuctionEntityListener;
import org.chzz.market.domain.base.entity.BaseTimeEntity;
import org.chzz.market.domain.product.entity.Product.Category;
import org.chzz.market.domain.user.entity.User;

/**
 * TODO: V2 경매 API 전환이 끝나서 운영 환경에 적용할 땐 기존 테이블에서 데이터를 이관해야 합니다.(flyway 스크립트)
 */
@Entity
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(value = AuctionEntityListener.class)
@Table(name = "auction_v2")
@Builder
public class AuctionV2 extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "auction_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;

    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column
    private Integer minPrice;

    @Column(nullable = false, columnDefinition = "varchar(30)")
    @Enumerated(EnumType.STRING)
    private Category category;

    @Column
    private LocalDateTime endDateTime;

    @Column(columnDefinition = "varchar(20)")
    @Enumerated(EnumType.STRING)
    private AuctionStatus status;

    @Column
    private Long winnerId;

    @Column
    private Integer likeCount;

    @Column
    private Integer bidCount;

    @Getter
    @AllArgsConstructor
    public enum AuctionStatus {
        PROCEEDING("진행중"),
        ENDED("종료"),
        PRE("사전");

        private final String description;
    }
}
