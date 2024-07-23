package org.chzz.market.domain.auction.repository;

import org.chzz.market.domain.auction.dto.AuctionResponse;
import org.chzz.market.domain.product.entity.Product.Category;
import org.chzz.market.domain.auction.entity.SortType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AuctionRepositoryCustom {
    Page<AuctionResponse> findAuctionsByCategory(Category category, SortType sortType, Long userId, Pageable pageable);
}
