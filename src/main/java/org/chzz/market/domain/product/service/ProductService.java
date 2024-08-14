package org.chzz.market.domain.product.service;

import lombok.RequiredArgsConstructor;
import org.chzz.market.domain.product.dto.MyProductResponse;
import org.chzz.market.domain.product.dto.ProductDetailsResponse;
import org.chzz.market.domain.product.dto.ProductResponse;
import org.chzz.market.domain.product.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import static org.chzz.market.domain.product.entity.Product.*;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ProductService {

    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);
    private final ProductRepository productRepository;

    /*
     * 카테고리별 사전 등록 상품 목록 조회
     */
    public Page<ProductResponse> getProductListByCategory(Category category, Long userId, Pageable pageable) {
        return productRepository.findProductsByCategory(category, userId, pageable);
    }

    /*
     * 상품 상세 정보 조회
     */
    public ProductDetailsResponse getProductDetails(Long productId, Long userId) {
        return productRepository.findProductDetailsById(productId, userId);
    }

    /*
     * 나의 사전 등록 상품 목록 조회
     */
    public Page<MyProductResponse> getMyProductList(Long userId, Pageable pageable) {
        return productRepository.findMyProductsByUserId(userId, pageable);
    }
}
