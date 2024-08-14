package org.chzz.market.domain.product.service;

import lombok.RequiredArgsConstructor;
import org.chzz.market.domain.product.dto.ProductResponse;
import org.chzz.market.domain.product.entity.Product;
import org.chzz.market.domain.product.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ProductService {

    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);
    private final ProductRepository productRepository;

    /**
     * 카테고리별 상품 목록 조회
     */
    public Page<ProductResponse> getProductListByCategory(
            Product.Category category,
            Long userId,
            Pageable pageable) {
        return productRepository.findProductsByCategory(category, userId, pageable);
    }
}
