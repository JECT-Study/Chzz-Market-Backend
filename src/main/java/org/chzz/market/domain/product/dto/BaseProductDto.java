package org.chzz.market.domain.product.dto;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public abstract class BaseProductDto {
    protected String productName;
    protected String imageUrl;
    protected Long likeCount;
    protected Integer minPrice;

    public BaseProductDto(String productName, String imageUrl, Long likeCount, Integer minPrice) {
        this.productName = productName;
        this.imageUrl = imageUrl;
        this.likeCount = likeCount;
        this.minPrice = minPrice;
    }
}
