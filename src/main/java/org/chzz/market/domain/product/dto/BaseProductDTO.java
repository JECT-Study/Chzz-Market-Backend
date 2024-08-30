package org.chzz.market.domain.product.dto;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public abstract class BaseProductDTO {
    protected String name;
    protected String cdnPath;
    protected Long likeCount;
    protected Integer minPrice;
    protected Boolean isLiked;

    public BaseProductDTO(String name, String cdnPath, Long likeCount, Integer minPrice, Boolean isLiked) {
        this.name = name;
        this.cdnPath = cdnPath;
        this.likeCount = likeCount;
        this.minPrice = minPrice;
        this.isLiked = isLiked;
    }
}
