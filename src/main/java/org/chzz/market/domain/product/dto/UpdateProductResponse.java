package org.chzz.market.domain.product.dto;

import java.util.List;
import org.chzz.market.domain.image.dto.ImageResponse;
import org.chzz.market.domain.product.entity.Product;
import org.chzz.market.domain.product.entity.Product.Category;

public record UpdateProductResponse(
        Long productId,
        String productName,
        String description,
        Category category,
        Integer minPrice,
        List<ImageResponse> imageUrls
) {
    public static UpdateProductResponse from(Product product) {
        return new UpdateProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getCategory(),
                product.getMinPrice(),
                product.getImages().stream()
                        .map(ImageResponse::from)
                        .toList()
        );
    }
}
