package org.chzz.market.domain.product.repository;

import org.chzz.market.domain.product.dto.ProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import static org.chzz.market.domain.product.entity.Product.*;

public interface ProductRepositoryCustom {
    Page<ProductResponse> findProductsByCategory(Category category, Long userId, Pageable pageable);
}
