package org.chzz.market.domain.notification.entity;

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
import org.chzz.market.domain.base.entity.BaseTimeEntity;
import org.chzz.market.domain.notification.error.NotificationErrorCode;
import org.chzz.market.domain.notification.error.NotificationException;
import org.chzz.market.domain.product.entity.Product;
import org.chzz.market.domain.user.entity.User;

@Getter
@Entity
@Table
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(nullable = false)
    private String message;

    @Column(nullable = false)
    private boolean isRead;

    @Column(nullable = false)
    private boolean isDeleted;

    @Column(nullable = false, columnDefinition = "varchar(30)")
    @Enumerated(EnumType.STRING)
    private Type type;

    public void read() {
        if(this.isRead) {
            throw new NotificationException(NotificationErrorCode.ALREADY_READ_NOTIFICATION);
        }
        if (this.isDeleted) {
            throw new NotificationException(NotificationErrorCode.DELETED_NOTIFICATION);
        }
        this.isRead = true;
    }

    public void delete() {
        if (this.isDeleted) {
            throw new NotificationException(NotificationErrorCode.DELETED_NOTIFICATION);
        }
        this.isDeleted = true;
    }

    @AllArgsConstructor
    public enum Type {
        AUCTION_START("좋아요를 누르신 사전 등록 제품 '%s'의 경매가 시작되었습니다."),
        AUCTION_SUCCESS("경매에 올린 '%s'가 낙찰되었습니다."),
        AUCTION_FAILURE("경매에 올린 '%s'가 미낙찰되었습니다."),
        AUCTION_WINNER("축하합니다! 입찰에 참여한 경매 '%s'의 낙찰자로 선정되었습니다."),
        AUCTION_NON_WINNER("안타깝지만 입찰에 참여한 경매 '%s'에 낙찰되지 못했습니다."),
        AUCTION_REGISTRATION_CANCELED("좋아요를 누른 사전 등록 제품 '%s'이(가) 판매자에 의해 취소되었습니다.");

        private final String message;

        public String getMessage(String productName) {
            return String.format(message, productName);
        }
    }
}
