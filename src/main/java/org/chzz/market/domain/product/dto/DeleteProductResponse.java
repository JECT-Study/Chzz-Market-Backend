package org.chzz.market.domain.product.dto;

import org.chzz.market.domain.auction.entity.Auction;
import org.chzz.market.domain.product.entity.Product;

public record DeleteProductResponse (
        Long productId,
        String productName,
        boolean wasAuctioned,
        int notifiedParticipants,
        String message
) {
    private static final String PRE_REGISTERED_DELETE_MESSAGE = "사전 등록 상품이 성공적으로 삭제되었습니다. 상품 ID: %d";
    private static final String AUCTIONED_DELETE_MESSAGE = "상품 ID: %d번, 경매 ID: %d번에 해당하는 경매 등록 상품이 성공적으로 삭제되었습니다. 총 %d 명에게 경매 취소 알림이 전송되었습니다.";

    public static DeleteProductResponse ofPreRegistered(Product product) {
        return new DeleteProductResponse(
                product.getId(),
                product.getName(),
                false,
                0,
                String.format(PRE_REGISTERED_DELETE_MESSAGE, product.getId())
        );
    }

    public static DeleteProductResponse ofAuctioned(Product product, Auction auction, int notifiedParticipants) {
        return new DeleteProductResponse(
                product.getId(),
                product.getName(),
                true,
                notifiedParticipants,
                String.format(AUCTIONED_DELETE_MESSAGE, product.getId(), auction.getId(), notifiedParticipants)
        );
    }
}
