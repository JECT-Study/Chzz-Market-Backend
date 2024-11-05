package org.chzz.market.domain.notification.entity;

import lombok.AllArgsConstructor;
import org.chzz.market.domain.notification.event.NotificationEvent;
import org.chzz.market.domain.user.entity.User;

@AllArgsConstructor
public enum NotificationType {
    AUCTION_SUCCESS("%s 경매가 낙찰되었습니다.", Values.AUCTION_SUCCESS) {
        @Override
        public Notification createNotification(Long userId, NotificationEvent event) {
            return new AuctionSuccessNotification(userId, event.cdnPath(), event.message(), event.getAuctionId());
        }
    },
    AUCTION_FAILURE("%s 경매가 유찰되었습니다.", Values.AUCTION_FAILURE) {
        @Override
        public Notification createNotification(Long userId, NotificationEvent event) {
            return new AuctionFailureNotification(userId, event.cdnPath(), event.message());
        }
    },
    AUCTION_WINNER("축하합니다! 입찰에 참여한 %s 경매에 낙찰되었습니다.", Values.AUCTION_WINNER) {
        @Override
        public Notification createNotification(Long userId, NotificationEvent event) {
            return new AuctionWinnerNotification(userId, event.cdnPath(), event.message(), event.getAuctionId());
        }
    },
    AUCTION_NON_WINNER("입찰에 참여한 %s 경매에 낙찰되지 못했습니다.", Values.AUCTION_NON_WINNER) {
        @Override
        public Notification createNotification(Long userId, NotificationEvent event) {
            return new AuctionNonWinnerNotification(userId, event.cdnPath(), event.message());
        }
    },
    AUCTION_START("좋아요를 누른 %s 경매가 시작되었습니다.", Values.AUCTION_START) {
        @Override
        public Notification createNotification(Long userId, NotificationEvent event) {
            return new AuctionStartNotification(userId, event.cdnPath(), event.message(), event.getAuctionId());
        }
    },
    PRE_AUCTION_CANCELED("좋아요를 누른 %s 사전 경매가 판매자에 의해 취소되었습니다.", Values.PRE_AUCTION_CANCELED) {
        @Override
        public Notification createNotification(Long userId, NotificationEvent event) {
            return new PreAuctionCanceledNotification(userId, event.cdnPath(), event.message());
        }
    };

    private final String message;
    private String value;

    public String getMessage(String productName) {
        return String.format(message, productName);
    }

    public abstract Notification createNotification(Long userId, NotificationEvent event);

    // 알림 상속관계 type 에 쓰이는 value 클래스
    public static class Values {
        public static final String AUCTION_START = "AUCTION_START";
        public static final String AUCTION_SUCCESS = "AUCTION_SUCCESS";
        public static final String AUCTION_FAILURE = "AUCTION_FAILURE";
        public static final String AUCTION_WINNER = "AUCTION_WINNER";
        public static final String AUCTION_NON_WINNER = "AUCTION_NON_WINNER";
        public static final String PRE_AUCTION_CANCELED = "PRE_AUCTION_CANCELED";
    }
}
