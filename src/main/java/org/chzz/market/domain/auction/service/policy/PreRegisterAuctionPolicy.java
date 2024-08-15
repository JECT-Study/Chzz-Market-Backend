package org.chzz.market.domain.auction.service.policy;

import org.chzz.market.domain.auction.dto.request.BaseRegisterRequest;
import org.chzz.market.domain.product.entity.Product;
import org.chzz.market.domain.user.entity.User;
import org.springframework.stereotype.Component;

@Component
public class PreRegisterAuctionPolicy extends AuctionPolicy {
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
}
