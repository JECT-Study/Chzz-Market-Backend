package org.chzz.market.domain.product.controller;

import lombok.RequiredArgsConstructor;
import org.chzz.market.domain.product.dto.DeleteProductResponse;
import org.chzz.market.domain.product.service.ProductService;
import org.springframework.http.HttpStatus;
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

    @PostMapping("/{productId}/delete")
    public ResponseEntity<DeleteProductResponse> deleteProduct(
            @PathVariable Long productId,
            @RequestHeader("X-User-Agent") Long userId) {
        DeleteProductResponse response = productService.deleteProduct(productId, userId);
        logger.info("상품이 성공적으로 삭제되었습니다. 상품 ID: {}", productId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

}
