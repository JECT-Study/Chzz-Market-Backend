package org.chzz.market.domain.auction.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.chzz.market.common.validation.annotation.ThousandMultiple;
import org.chzz.market.domain.base.entity.BaseTimeEntity;
import org.chzz.market.domain.product.entity.Product;
import org.chzz.market.domain.user.entity.User;

@Getter
@Entity
@Table
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Auction extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "auction_id")
    private Long id;

    @OneToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = true)
    private User winner;

    @Column
    @ThousandMultiple
    private Integer minPrice;

    @Column
    private LocalDateTime endDateTime;

    @Column(columnDefinition = "varchar(20)")
    @Enumerated(EnumType.STRING)
    private Status status;

    // 경매가 진행 중인지 확인
    public boolean isProceeding() {
        return status == Status.PROCEEDING;
    }

    // 경매가 종료되었는지 확인
    public boolean isEnded() {
        return LocalDateTime.now().isAfter(endDateTime);
    }

    // 입찰 금액이 최소 금액 이상인지 확인
    public boolean isAboveMinPrice(Long amount) {
        return amount >= minPrice;
    }

    @Getter
    @AllArgsConstructor
    public enum Status {
        PENDING("대기 중"),
        PROCEEDING("진행 중"),
        ENDED("종료"),
        CANCELLED("취소 됨");

        private final String description;
    }
}
