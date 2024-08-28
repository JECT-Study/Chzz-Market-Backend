package org.chzz.market.domain.product.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.chzz.market.domain.product.dto.DeleteProductResponse;
import org.chzz.market.domain.product.dto.UpdateProductRequest;
import org.chzz.market.domain.product.dto.UpdateProductResponse;
import org.chzz.market.domain.product.service.ProductService;
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
     * 상품 삭제
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
