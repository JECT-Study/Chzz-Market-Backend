package org.chzz.market.domain.auction.service.policy;

import org.chzz.market.domain.auction.dto.request.BaseRegisterRequest;
import org.chzz.market.domain.auction.entity.Auction;
import org.chzz.market.domain.product.entity.Product;
import org.chzz.market.domain.user.entity.User;
import org.springframework.stereotype.Component;

@Component
public class PreRegisterAuctionPolicy implements AuctionPolicy {
    @Override
    public Product createProduct(BaseRegisterRequest request, User user) {
        return Product.builder()
                .user(user)
                .name(request.getProductName())
                .minPrice(request.getMinPrice())
                .description(request.getDescription())
                .category(request.getCategory())
                .build();
    }

    @Override
    public Auction createAuction(Product product, BaseRegisterRequest request) {
        // 사전 등록에는 경매 생성하지 않음
        return null;
    }
}
