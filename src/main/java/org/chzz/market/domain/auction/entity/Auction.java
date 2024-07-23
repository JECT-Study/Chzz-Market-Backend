package org.chzz.market.domain.auction.entity;

import jakarta.persistence.CascadeType;
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
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.chzz.market.domain.base.entity.BaseTimeEntity;
import org.chzz.market.domain.product.entity.Product;
import org.chzz.market.domain.user.entity.User;

@Getter
@Entity
@Table
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Auction extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "auction_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY,cascade = CascadeType.ALL)
    @JoinColumn(name = "product_id")
    private Product product;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id",nullable = true)
    private User winner;

    @Column
    //TODO 2024 07 24 00:10:13 :  custom validate
    private Long minPrice;

    @Column(columnDefinition = "varchar(20)")
    @Enumerated(EnumType.STRING)
    private Status status;

    public enum Status {
        //TODO 2024 07 18 14:07:49 : 경매 상태 구체화
        DEFAULT
    }
}
