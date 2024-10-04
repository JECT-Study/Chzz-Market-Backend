package org.chzz.market.domain.product.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import org.chzz.market.domain.product.entity.Product.Category;

/**
 * 사전 등록 상품 상세 조회 DTO
 */
@Getter
public class ProductDetailsResponse {
    private final Long productId;
    private final String productName;
    private final String sellerNickname;
    private final Integer minPrice;
    private final LocalDateTime createdAt;
    private final String description;
    private final Long likeCount;
    private final Boolean isLiked;
    private final Boolean isSeller;
    private final Category category;
    private List<String> imageUrls;

    @QueryProjection
    public ProductDetailsResponse(Long productId, String productName, String sellerNickname,
                                  Integer minPrice, LocalDateTime createdAt, String description,
                                  Long likeCount, Boolean isLiked, Boolean isSeller, Category category) {
        this.productId = productId;
        this.productName = productName;
        this.sellerNickname = sellerNickname;
        this.minPrice = minPrice;
        this.createdAt = createdAt;
        this.description = description;
        this.likeCount = likeCount;
        this.isLiked = isLiked;
        this.isSeller = isSeller;
        this.category = category;
    }

    public void addImageList(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }
}
