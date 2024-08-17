package org.chzz.market.domain.auction.service.policy;

import org.chzz.market.domain.auction.dto.request.BaseRegisterRequest;
import org.chzz.market.domain.auction.entity.Auction;
import org.chzz.market.domain.product.entity.Product;
import org.chzz.market.domain.user.entity.User;

import static org.chzz.market.domain.auction.entity.Auction.*;
import static org.chzz.market.domain.auction.entity.Auction.AuctionStatus.*;

public abstract class AuctionPolicy {
    public abstract Product createProduct(BaseRegisterRequest request, User user);

    public Auction createAuction(Product product, BaseRegisterRequest request) {
        return builder()
                .product(product)
                .status(PROCEEDING)
                .build();
    }
}
