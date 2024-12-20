package org.chzz.market.domain.auction.entity;

import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Mapping;
import org.springframework.data.elasticsearch.annotations.Setting;

@Document(indexName = "auction")
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Setting(settingPath = "elastic/auction-setting.json")
@Mapping(mappingPath = "elastic/auction-mapping.json")
public class AuctionDocument {
    @Id
    private Long auctionId;
    private Long sellerId;
    private String name;
    private String description;
    private Integer minPrice;
    private String category;
    private AuctionStatus auctionStatus;
    private String imageUrl;
    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
    private LocalDateTime endDateTime;
    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
    private LocalDateTime createAt;

    public static AuctionDocument from(Auction auction) {
        return AuctionDocument.builder()
                .auctionId(auction.getId())
                .sellerId(auction.getSeller().getId())
                .name(auction.getName())
                .description(auction.getDescription())
                .minPrice(auction.getMinPrice())
                .category(auction.getCategory().getDisplayName())
                .auctionStatus(auction.getStatus())
                .imageUrl(auction.getFirstImageCdnPath())
                .endDateTime(auction.getEndDateTime())
                .createAt(auction.getCreatedAt())
                .build();
    }

    @Getter
    @AllArgsConstructor
    public class Constant {
        public static final String NAME = "name";
        public static final String DESCRIPTION = "description";
        public static final String MIN_PRICE = "minPrice";
        public static final String CATEGORY = "category";
        public static final String IMAGE_URL = "imageUrl";
        public static final String AUCTION_STATUS = "auctionStatus";
        public static final String END_DATE_TIME = "endDateTime";
        public static final String CREATE_AT = "createAt";
    }
}
