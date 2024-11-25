package org.chzz.market.domain.auction.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.chzz.market.common.validation.annotation.ThousandMultiple;
import org.chzz.market.domain.auction.dto.AuctionRegisterType;
import org.chzz.market.domain.auction.entity.Category;

public record RegisterRequest(
        String productName,

        @Schema(description = "개행문자 포함 최대 1000자, 개행문자 최대 10개")
        @Size(max = 1000, message = "상품설명은 1000자 이내여야 합니다.")
        @Pattern(regexp = DESCRIPTION_REGEX, message = "줄 바꿈 10번까지 가능합니다")
        String description,

        @NotNull(message = "카테고리를 선택해주세요")
        Category category,

        @NotNull
        @ThousandMultiple
        @Max(value = 2_000_000, message = "최소금액은 200만원을 넘을 수 없습니다")
        Integer minPrice,

        @NotNull(message = "경매 타입을 선택해주세요")
        AuctionRegisterType auctionRegisterType
) {
    private static final String DESCRIPTION_REGEX = "^(?:(?:[^\\n]*\\n){0,10}[^\\n]*$)"; // 개행문자 10개를 제한
}
