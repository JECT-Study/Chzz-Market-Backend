package org.chzz.market.domain.product.repository;

import org.chzz.market.domain.product.dto.ProductDetailsResponse;
import org.chzz.market.domain.product.dto.ProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import static org.chzz.market.domain.product.entity.Product.*;

public interface ProductRepositoryCustom {
    /**
     * 카테고리와 정렬 조건에 따라 사전 등록 상품 리스트를 조회합니다.
     * @param category
     * @param userId
     * @param pageable
     * @return
     */
    Page<ProductResponse> findProductsByCategory(Category category, Long userId, Pageable pageable);

    /**
     * 사용자 ID와 상품 ID에 따라 사전 등록 상품 상세 정보를 조회합니다.
     * @param productId 상품 ID
     * @param userId    사용자 ID
     * @return          상품 상세 정보
     */
    ProductDetailsResponse findProductDetailsById(Long productId, Long userId);

}
