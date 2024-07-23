package org.chzz.market.domain.auction.service;

import lombok.RequiredArgsConstructor;
import org.chzz.market.domain.auction.dto.AuctionResponse;
import org.chzz.market.domain.auction.repository.AuctionRepository;
import org.chzz.market.domain.product.entity.Product.Category;
import org.chzz.market.domain.auction.entity.SortType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AuctionService {
    private final AuctionRepository auctionRepository;

    public Page<AuctionResponse> getAuctionListByCategory(Category category, SortType sortType, Long userId,
                                                          Pageable pageable) {
        return auctionRepository.findAuctionsByCategory(category, sortType, userId, pageable);
    }
}
