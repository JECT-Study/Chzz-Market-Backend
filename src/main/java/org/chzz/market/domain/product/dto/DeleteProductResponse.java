package org.chzz.market.domain.product.dto;

import org.chzz.market.domain.product.entity.Product;

public record DeleteProductResponse (
        Long productId,
        String productName,
        int likeCount,
        String message
) {
    private static final String DELETE_SUCCESS_MESSAGE = "사전 등록 상품이 성공적으로 삭제되었습니다. 상품 ID: %d, 좋아요 누름 사용자 수: %d";

    public static DeleteProductResponse ofPreRegistered(Product product, int likeCount) {
        return new DeleteProductResponse(
                product.getId(),
                product.getName(),
                likeCount,
                String.format(DELETE_SUCCESS_MESSAGE, product.getId(), likeCount)
        );
    }
}
