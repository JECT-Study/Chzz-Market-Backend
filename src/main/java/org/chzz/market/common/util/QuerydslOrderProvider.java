package org.chzz.market.common.util;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class QuerydslOrderProvider {
    private final QuerydslOrderRegistry querydslOrderRegistry;

    public Optional<QuerydslOrder> findOrderByName(String name) {
        return Optional.ofNullable(querydslOrderRegistry.getOrderByName(name));
    }

    public OrderSpecifier<?>[] getOrderSpecifiers(Pageable pageable) {
        List<OrderSpecifier<?>> orderSpecifiers = pageable.getSort().stream()
                .map(this::createOrderSpecifier)
                .flatMap(Optional::stream)
                .collect(Collectors.toList());

        if (orderSpecifiers.isEmpty()) {
            orderSpecifiers.add(OrderByNull.DEFAULT);
        }

        return orderSpecifiers.toArray(OrderSpecifier[]::new);
    }

    private Optional<OrderSpecifier<?>> createOrderSpecifier(Sort.Order order) {
        return findOrderByName(order.getProperty())
                .map(querydslOrder -> buildOrderSpecifier(querydslOrder.getOrderSpecifier(), order.isAscending()));
    }

    private OrderSpecifier<?> buildOrderSpecifier(OrderSpecifier<?> baseOrderSpecifier, boolean isAscending) {
        // 오름차순 정렬 시 기본 OrderSpecifier 사용
        if (isAscending) {
            return baseOrderSpecifier;
        }

        // 내림차순 정렬 시 반대 방향의 OrderSpecifier 생성
        Order oppositeOrder = (baseOrderSpecifier.getOrder() == Order.ASC) ? Order.DESC : Order.ASC;
        return new OrderSpecifier<>(oppositeOrder, baseOrderSpecifier.getTarget());
    }
}
