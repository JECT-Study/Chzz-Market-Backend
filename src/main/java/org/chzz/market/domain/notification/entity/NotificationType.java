package org.chzz.market.domain.notification.entity;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum NotificationType {
    AUCTION_START("LikedAuctionStarted", "좋아요를 누르신 사전 등록 제품 '%s'의 경매가 시작되었습니다."),
    AUCTION_SUCCESS("AuctionItemSold", "경매에 올린 '%s'가 낙찰되었습니다."),
    AUCTION_FAILURE("AuctionItemUnSold", "경매에 올린 '%s'가 미낙찰되었습니다."),
    AUCTION_WINNER("AuctionBidWon", "축하합니다! 입찰에 참여한 경매 '%s'의 낙찰자로 선정되었습니다."),
    AUCTION_NON_WINNER("AuctionBidFailed", "안타깝지만 입찰에 참여한 경매 '%s'에 낙찰되지 못했습니다."),
    AUCTION_REGISTRATION_CANCELED("LikedAuctionCanceled", "좋아요를 누른 사전 등록 제품 '%s'이(가) 판매자에 의해 취소되었습니다.");

    private final String name;
    private final String message;

    public String getName() {
        return name;
    }

    public String getMessage(String productName) {
        return String.format(message, productName);
    }
}
