package org.chzz.market.domain.auction.service;

import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.chzz.market.common.util.StringCaseConverter;
import org.chzz.market.domain.auction.dto.response.CategoryResponse;
import org.chzz.market.domain.auction.entity.Category;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuctionCategoryService {

    /**
     * 경매의 카테고리를 조회
     */
    public List<CategoryResponse> getCategories() {
        return Arrays.stream(Category.values())
                .map(category -> new CategoryResponse(StringCaseConverter.toLowerCaseWithHyphens(category.name()),
                        category.getDisplayName()))
                .toList();
    }
}
