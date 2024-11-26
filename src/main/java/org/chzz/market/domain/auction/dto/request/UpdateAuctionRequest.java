package org.chzz.market.domain.auction.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.chzz.market.common.validation.annotation.ThousandMultiple;
import org.chzz.market.domain.auction.entity.Category;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAuctionRequest {
    public static final String DESCRIPTION_REGEX = "^(?:(?:[^\\n]*\\n){0,10}[^\\n]*$)"; // 개행문자 10개를 제한

    @Size(min = 2, max = 30, message = "제목은 최소 2글자 이상 30자 이하여야 합니다")
    private String productName;

    @Schema(description = "개행문자 포함 최대 1000자, 개행문자 최대 10개")
    @Size(max = 1000, message = "상품설명은 1000자 이내여야 합니다.")
    @Pattern(regexp = DESCRIPTION_REGEX, message = "줄 바꿈 10번까지 가능합니다")
    protected String description;

    private Category category;

    @ThousandMultiple
    @Max(value = 2_000_000, message = "최소금액은 200만원을 넘을 수 없습니다")
    private Integer minPrice;

    @Builder.Default
    private Map<Long, Integer> imageSequence = new HashMap<>();
}

