package org.chzz.market.domain.auction.service.policy;

import org.chzz.market.domain.auction.dto.request.BaseRegisterRequest;
import org.chzz.market.domain.auction.entity.Auction;
import org.chzz.market.domain.product.entity.Product;
import org.chzz.market.domain.user.entity.User;

public interface AuctionPolicy {
    Product createProduct(BaseRegisterRequest request, User user);
    Auction createAuction(Product product, BaseRegisterRequest request);
}
