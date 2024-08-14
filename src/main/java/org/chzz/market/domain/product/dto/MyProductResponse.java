package org.chzz.market.domain.product.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 나의 사전 등록 상품 조회 DTO
 */
@Getter
public class MyProductResponse extends BaseProductDTO {
    private final Long id;
    private final LocalDateTime createdAt;

    @QueryProjection
    public MyProductResponse(Long id, String name, String cdnPath, Long likeCount,
                             Integer minPrice, Boolean isLiked, LocalDateTime createdAt) {
        super(name, cdnPath, likeCount, minPrice, isLiked);
        this.id = id;
        this.createdAt = createdAt;
    }
}
