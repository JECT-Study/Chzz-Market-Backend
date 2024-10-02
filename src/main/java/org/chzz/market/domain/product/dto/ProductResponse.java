package org.chzz.market.domain.product.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;


/**
 * 사전 등록 상품 목록 조회 DTO
 */
@Getter
public class ProductResponse extends BaseProductDto {
    private final Long productId;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean isLiked;

    @QueryProjection
    public ProductResponse(Long productId, String name, String cdnPath, Integer minPrice,
                           Long likeCount, Boolean isLiked) {
        super(name, cdnPath, likeCount, minPrice);
        this.productId = productId;
        this.isLiked = isLiked;
    }

    @QueryProjection
    public ProductResponse(Long productId, String name, String cdnPath, Integer minPrice,
                           Long likeCount) {
        super(name, cdnPath, likeCount, minPrice);
        this.productId = productId;
    }
}
