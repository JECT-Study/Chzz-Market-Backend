package org.chzz.market.domain.payment.entity;

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
import org.chzz.market.domain.auction.entity.Auction;
import org.chzz.market.domain.base.entity.BaseTimeEntity;
import org.chzz.market.domain.user.entity.User;

@Getter
@Entity
@Table
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User payer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auction_id", nullable = false)
    private Auction auction;

    @Column(nullable = false)//TODO 2024 07 18 14:07:01 : custom validation
    private Long amount;

    @Column(columnDefinition = "varchar(30)", nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentMethod method;

    @Column(columnDefinition = "varchar(30)", nullable = false)
    @Enumerated(EnumType.STRING)
    private Status status;

    @PrePersist
    protected void onPrePersist() {
        if (this.status == null) {
            this.status = Status.READY;
        }
    }


    public enum PaymentMethod {
        CARD, CASH // Test 실행을 위해 임시 추가
    }

    @Getter
    @AllArgsConstructor
    public enum Status {
        READY("ready"),
        CANCELED("canceled"),
        APPROVED("approve"),
        PAYED("payed");
        private final String name;
    }
}
