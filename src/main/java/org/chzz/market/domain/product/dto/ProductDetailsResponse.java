package org.chzz.market.domain.product.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 사전 등록 상품 상세 조회 DTO
 */
@Getter
public class ProductDetailsResponse {
    private final Long productId;
    private final String productName;
    private final String sellerName;
    private final Integer minPrice;
    private final LocalDateTime createdAt;
    private final String description;
    private final Long likeCount;
    private final boolean isLiked;
    private List<String> imageUrls;

    @QueryProjection
    public ProductDetailsResponse(Long productId, String productName, String sellerName,
                                  Integer minPrice, LocalDateTime createdAt, String description,
                                  Long likeCount, boolean isLiked) {
        this.productId = productId;
        this.productName = productName;
        this.sellerName = sellerName;
        this.minPrice = minPrice;
        this.createdAt = createdAt;
        this.description = description;
        this.likeCount = likeCount;
        this.isLiked = isLiked;
    }

    public void addImageList(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }
}
