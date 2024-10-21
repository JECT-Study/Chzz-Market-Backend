package org.chzz.market.domain.order.entity;

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
import org.chzz.market.domain.address.entity.Address;
import org.chzz.market.domain.auction.entity.Auction;
import org.chzz.market.domain.payment.entity.Payment;
import org.chzz.market.domain.payment.entity.Payment.PaymentMethod;

@Entity
@Getter
@Builder
@Table(name = "orders")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long id;

    @Column(nullable = false)
    private String orderNo;

    @Column(nullable = false)
    private Long buyerId;

    @Column(nullable = false)
    private Long paymentId;

    @Column(nullable = false)
    private Long amount;

    @Column
    private String deliveryMemo;

    @Column(nullable = false)
    private String roadAddress;

    @Column(nullable = false)
    private String jibun;

    @Column(nullable = false)
    private String zipcode;

    @Column(nullable = false)
    private String detailAddress;

    @Column(nullable = false)
    private String recipientName;

    @Column(nullable = false)
    private String phoneNumber;

    @Column(columnDefinition = "varchar(30)", nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentMethod method;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auction_id")
    private Auction auction;

    public static Order of(Long userId, Payment payment, Address address, String deliveryMemo) {
        return Order.builder()
                .buyerId(userId)
                .paymentId(payment.getId())
                .orderNo(payment.getOrderId())
                .amount(payment.getAmount())
                .auction(payment.getAuction())
                .method(payment.getMethod())
                .roadAddress(address.getRoadAddress())
                .jibun(address.getJibun())
                .zipcode(address.getZipcode())
                .detailAddress(address.getDetailAddress())
                .recipientName(address.getRecipientName())
                .phoneNumber(address.getPhoneNumber())
                .deliveryMemo(deliveryMemo)
                .build();
    }
}
