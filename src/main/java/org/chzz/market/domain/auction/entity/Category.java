package org.chzz.market.domain.auction.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Category {
    ELECTRONICS("전자기기"),
    HOME_APPLIANCES("가전제품"),
    FASHION_AND_CLOTHING("패션 및 의류"),
    FURNITURE_AND_INTERIOR("가구 및 인테리어"),
    BOOKS_AND_MEDIA("도서 및 미디어"),
    SPORTS_AND_LEISURE("스포츠 및 레저"),
    TOYS_AND_HOBBIES("장난감 및 취미"),
    OTHER("기타");

    private final String displayName;
}
