package org.chzz.market.domain.product.dto;

import static org.chzz.market.domain.auction.dto.request.BaseRegisterRequest.DESCRIPTION_REGEX;
import static org.chzz.market.domain.product.entity.Product.Category;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.chzz.market.common.validation.annotation.ThousandMultiple;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProductRequest {
    @Size(min = 2, max = 30, message = "제목은 최소 2글자 이상 30자 이하여야 합니다")
    private String productName;

    @Schema(description = "개행문자 포함 최대 1000자, 개행문자 최대 10개")
    @Size(max = 1000, message = "상품설명은 1000자 이내여야 합니다.")
    @Pattern(regexp = DESCRIPTION_REGEX, message = "줄 바꿈 10번까지 가능합니다")
    protected String description;

    private Category category;

    @ThousandMultiple
    @Min(value = 1000, message = "시작 가격은 최소 1,000원 이상, 1000의 배수이어야 합니다")
    private Integer minPrice;

    @Builder.Default
    private Map<Long, Integer> imageSequence = new HashMap<>();
}
