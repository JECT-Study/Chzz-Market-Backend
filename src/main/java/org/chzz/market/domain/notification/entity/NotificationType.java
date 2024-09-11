package org.chzz.market.domain.notification.entity;

import lombok.AllArgsConstructor;
import org.chzz.market.domain.notification.event.NotificationEvent;
import org.chzz.market.domain.user.entity.User;

@AllArgsConstructor
public enum NotificationType {
    AUCTION_START("좋아요를 누르신 사전 등록 제품 '%s'의 경매가 시작되었습니다.", Values.AUCTION_START) {
        @Override
        public Notification createNotification(User user, NotificationEvent event) {
            return new AuctionStartNotification(user, event.image(), event.message(), event.getAuctionId());
        }
    },
    AUCTION_SUCCESS("경매에 올린 '%s'가 낙찰되었습니다.", Values.AUCTION_SUCCESS) {
        @Override
        public Notification createNotification(User user, NotificationEvent event) {
            return new AuctionSuccessNotification(user, event.image(), event.message(), event.getAuctionId());
        }
    },
    AUCTION_FAILURE("경매에 올린 '%s'가 미낙찰되었습니다.", Values.AUCTION_FAILURE) {
        @Override
        public Notification createNotification(User user, NotificationEvent event) {
            return new AuctionFailureNotification(user, event.image(), event.message());
        }
    },
    AUCTION_WINNER("축하합니다! 입찰에 참여한 경매 '%s'의 낙찰자로 선정되었습니다.", Values.AUCTION_WINNER) {
        @Override
        public Notification createNotification(User user, NotificationEvent event) {
            return new AuctionWinnerNotification(user, event.image(), event.message(), event.getAuctionId());
        }
    },
    AUCTION_NON_WINNER("안타깝지만 입찰에 참여한 경매 '%s'에 낙찰되지 못했습니다.", Values.AUCTION_NON_WINNER) {
        @Override
        public Notification createNotification(User user, NotificationEvent event) {
            return new AuctionNonWinnerNotification(user, event.image(), event.message());
        }
    },
    AUCTION_REGISTRATION_CANCELED("좋아요를 누른 사전 등록 제품 '%s'이(가) 판매자에 의해 취소되었습니다.", Values.AUCTION_REGISTRATION_CANCELED) {
        @Override
        public Notification createNotification(User user, NotificationEvent event) {
            return new AuctionRegistrationCanceledNotification(user, event.image(), event.message());
        }
    };

    private final String message;
    private String value;

    public String getMessage(String productName) {
        return String.format(message, productName);
    }

    public abstract Notification createNotification(User user, NotificationEvent event);

    // 알림 상속관계 type 에 쓰이는 value 클래스
    public static class Values {
        public static final String AUCTION_START = "AUCTION_START";
        public static final String AUCTION_SUCCESS = "AUCTION_SUCCESS";
        public static final String AUCTION_FAILURE = "AUCTION_FAILURE";
        public static final String AUCTION_WINNER = "AUCTION_WINNER";
        public static final String AUCTION_NON_WINNER = "AUCTION_NON_WINNER";
        public static final String AUCTION_REGISTRATION_CANCELED = "AUCTION_REGISTRATION_CANCELED";
    }
}
