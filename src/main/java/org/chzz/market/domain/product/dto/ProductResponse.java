package org.chzz.market.domain.product.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;

/**
 * 사전 등록 상품 목록 조회 DTO
 */
@Getter
public class ProductResponse extends BaseProductDTO {
    private final Long id;
    private final Boolean isLiked;

    @QueryProjection
    public ProductResponse(Long id, String name, String cdnPath, Integer minPrice,
                               Long likeCount, Boolean isLiked) {
        super(name, cdnPath, likeCount, minPrice, isLiked);
        this.id = id;
        this.isLiked = isLiked;
    }
}
