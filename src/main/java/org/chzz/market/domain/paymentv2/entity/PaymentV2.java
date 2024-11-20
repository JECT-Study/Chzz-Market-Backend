package org.chzz.market.domain.paymentv2.entity;

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
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.chzz.market.domain.auctionv2.entity.AuctionV2;
import org.chzz.market.domain.base.entity.BaseTimeEntity;
import org.chzz.market.domain.user.entity.User;

@Getter
@Entity
@Table
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentV2 extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User payer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auction_id", nullable = false)
    private AuctionV2 auction;

    @Column(nullable = false)
    private Long amount;

    @Column(columnDefinition = "varchar(30)", nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentMethod method;

    @Column(columnDefinition = "varchar(30)", nullable = false)
    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(unique = true, nullable = false)
    private String orderNo;

    @Column(nullable = false)
    private String paymentKey;

    @PrePersist
    protected void onPrePersist() {
        if (this.status == null) {
            this.status = Status.READY;
        }
    }

    @AllArgsConstructor
    public enum PaymentMethod {
        CARD("카드"),
        VIRTUAL_ACCOUNT("가상계좌"),
        EASY_PAYMENT("간편결제"),
        MOBILE("휴대폰"),
        ACCOUNT_TRANSFER("계좌이체"),
        CULTURE_GIFT_CARD("문화상품권"),
        BOOK_CULTURE_GIFT_CARD("도서문화상품권"),
        GAME_CULTURE_GIFT_CARD("게임문화상품권"),
        CASH("테스트용");

        private final String description;
    }
}
