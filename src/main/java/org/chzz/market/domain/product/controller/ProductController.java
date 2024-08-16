package org.chzz.market.domain.product.controller;

import lombok.RequiredArgsConstructor;
import org.chzz.market.domain.product.dto.ProductDetailsResponse;
import org.chzz.market.domain.product.dto.ProductResponse;
import org.chzz.market.domain.product.entity.Product.Category;
import org.chzz.market.domain.product.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RequiredArgsConstructor
@RestController
@RequestMapping("/products")
public class ProductController {
    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);

    private final ProductService productService;

    /*
     * 카테고리 별 사전 등록 상품 목록 조회
     */
    // TODO: 추후에 인증된 사용자 정보로 수정 필요
    @GetMapping
    public ResponseEntity<Page<ProductResponse>> getProductList(
            @RequestParam Category category,
            @RequestHeader("X-User-Agent") Long userId,
            Pageable pageable) {
        return ResponseEntity.ok(productService.getProductListByCategory(category, userId, pageable)); // 임의의 사용자 ID
    }

    /*
     * 사전 등록 상품 상세 정보 조회
     */
    // TODO: 추후에 인증된 사용자 정보로 수정 필요
    @GetMapping("/{productId}")
    public ResponseEntity<ProductDetailsResponse> getProductDetails(
            @PathVariable Long productId,
            @RequestHeader("X-User-Agent") Long userId) {
        return productService.getProductDetails(productId, userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /*
     * 나의 사전 등록 상품 목록 조회
     */
    // TODO: 추후에 인증된 사용자 정보로 수정 필요
    @GetMapping("/my-products")
    public ResponseEntity<Page<ProductResponse>> getMyProductList(
            @RequestHeader("X-User-Agent") Long userId,
            Pageable pageable) {
        return ResponseEntity.ok(productService.getMyProductList(userId, pageable));
    }
}