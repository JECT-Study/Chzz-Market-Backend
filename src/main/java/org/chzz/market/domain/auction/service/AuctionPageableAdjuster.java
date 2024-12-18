package org.chzz.market.domain.auction.service;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Component;

@Component
public class AuctionPageableAdjuster {
    private static final Map<String, Order> SORT_REGISTRY = new HashMap<>();

    static {
        SORT_REGISTRY.put("newest", Sort.Order.desc("createAt"));  // newest -> createdAt DESC
        SORT_REGISTRY.put("cheap", Sort.Order.asc("minPrice"));    // cheap -> minPrice ASC
        SORT_REGISTRY.put("expensive", Sort.Order.desc("minPrice"));// expensive -> minPrice DESC
    }

    /**
     * Pageable 조정
     */
    public Pageable adjustPageable(Pageable pageable) {
        Sort adjustedSort = Sort.by(pageable.getSort().stream()
                .map(this::mapAndReverseSort)
                .collect(Collectors.toList()));

        // 정렬 조건이 없으면 기본 정렬 조건 설정
        if (adjustedSort.isEmpty()) {
            adjustedSort = Sort.by(SORT_REGISTRY.get("newest"));
        }

        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), adjustedSort);
    }

    /**
     * 요청된 정렬 조건을 매핑 및 방향 변환
     */
    private Sort.Order mapAndReverseSort(Sort.Order order) {
        Sort.Order mappedOrder = SORT_REGISTRY.get(order.getProperty());

        if (mappedOrder != null) {
            if (order.getDirection().isAscending()) {
                return mappedOrder;
            }
            Direction direction = mappedOrder.getDirection().isAscending() ? Direction.DESC : Direction.ASC;
            return new Sort.Order(direction, mappedOrder.getProperty());
        }

        Sort.Order defaultOrder = SORT_REGISTRY.get("newest");
        return new Sort.Order(defaultOrder.getDirection(), defaultOrder.getProperty());
    }
}
