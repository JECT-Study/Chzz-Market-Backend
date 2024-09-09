package org.chzz.market.domain.product.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.chzz.market.common.validation.annotation.ThousandMultiple;

import static org.chzz.market.domain.product.entity.Product.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProductRequest {
    @Size(min = 2, message = "제목은 최소 2글자 이상이어야 합니다")
    private String name;

    @Size(max = 1000, message = "상품 설명은 최대 1000자까지 가능합니다")
    private String description;

    private Category category;

    @ThousandMultiple
    @Min(value = 1000, message = "시작 가격은 최소 1,000원 이상, 1000의 배수이어야 합니다")
    private Integer minPrice;
}
