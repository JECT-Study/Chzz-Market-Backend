package org.chzz.market.domain.product.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.chzz.market.domain.product.dto.*;
import org.chzz.market.domain.product.entity.Product;
import org.chzz.market.domain.product.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

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
            @RequestParam Product.Category category,
            @RequestHeader("X-User-Agent") Long userId,
            Pageable pageable) {
        return ResponseEntity.ok(productService.getProductListByCategory(category, userId, pageable)); // 임의의 사용자 ID
    }

    /*
     * 상품 카테고리 목록 조회
     */
    @GetMapping("/categories")
    public ResponseEntity<List<CategoryResponse>> getCategoryList() {
        return ResponseEntity.ok(productService.getCategories());
    }

    /*
     * 사전 등록 상품 상세 정보 조회
     */
    // TODO: 추후에 인증된 사용자 정보로 수정 필요
    @GetMapping("/{productId}")
    public ResponseEntity<ProductDetailsResponse> getProductDetails(
            @PathVariable Long productId,
            @RequestHeader("X-User-Agent") Long userId) {
        ProductDetailsResponse response = productService.getProductDetails(productId, userId);
        return ResponseEntity.ok(response);
    }

    /*
     * 나의 사전 등록 상품 목록 조회
     */
    // TODO: 추후에 인증된 사용자 정보로 수정 필요
    @GetMapping("/user/{nickname}")
    public ResponseEntity<Page<ProductResponse>> getMyProductList(
            @PathVariable String nickname,
            Pageable pageable) {
        return ResponseEntity.ok(productService.getMyProductList(nickname, pageable));
    }

    /**
     * 사전 등록 상품 수정
     */
    @PatchMapping("/{productId}")
    public ResponseEntity<UpdateProductResponse> updateProduct(
            @PathVariable Long productId,
            @RequestPart("request") @Valid UpdateProductRequest request,
            @RequestPart(value = "images", required = false) List<MultipartFile> images) {
        UpdateProductResponse response = productService.updateProduct(productId, request, images);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * 사전 등록 상품 삭제
     */
    @DeleteMapping("/{productId}")
    public ResponseEntity<DeleteProductResponse> deleteProduct(
            @PathVariable Long productId,
            @RequestHeader("X-User-Agent") Long userId) {
        DeleteProductResponse response = productService.deleteProduct(productId, userId);
        logger.info("상품이 성공적으로 삭제되었습니다. 상품 ID: {}", productId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

}
