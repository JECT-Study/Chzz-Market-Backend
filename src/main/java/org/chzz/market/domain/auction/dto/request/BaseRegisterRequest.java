package org.chzz.market.domain.auction.dto.request;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.chzz.market.common.validation.annotation.ThousandMultiple;
import org.chzz.market.domain.auction.type.AuctionRegisterType;

import static org.chzz.market.domain.product.entity.Product.*;

@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "auctionRegisterType", visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(name = "PRE_REGISTER", value = PreRegisterRequest.class),
        @JsonSubTypes.Type(name = "REGISTER", value = RegisterAuctionRequest.class)
})
public abstract class BaseRegisterRequest {
    @NotNull
    protected Long userId;

    @NotBlank
    @Size(min = 2, message = "제목은 최소 2글자 이상이어야 합니다")
    protected String productName;

    @NotNull
    @Size(max = 1000, message = "상품 설명은 최대 1000자까지 가능합니다")
    protected String description;

    @NotNull(message = "카테고리를 선택해주세요")
    protected Category category;

    @NotNull
    @ThousandMultiple
    @Min(value = 1000, message = "시작 가격은 최소 1,000원 이상, 1000의 배수이어야 합니다")
    protected Integer minPrice;

    @NotNull(message = "경매 타입을 선택해주세요")
    protected AuctionRegisterType auctionRegisterType;
}
