package org.chzz.market.domain.product.dto;

import org.chzz.market.domain.image.entity.Image;
import org.chzz.market.domain.product.entity.Product;

import java.util.List;

public record UpdateProductResponse (
        Long id,
        String name,
        String description,
        Product.Category category,
        Integer minPrice,
        List<String> cdnPaths
) {
    public static UpdateProductResponse from(Product product) {
        return new UpdateProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getCategory(),
                product.getMinPrice(),
                product.getImages().stream()
                        .map(Image::getCdnPath)
                        .toList()
        );
    }
}
