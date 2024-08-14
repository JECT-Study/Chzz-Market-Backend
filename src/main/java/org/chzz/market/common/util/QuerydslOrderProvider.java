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

    public Optional<QuerydslOrder> getByName(String name) {
        return Optional.of(querydslOrderRegistry.getOrderByName(name));
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
        Order direction = order.isAscending() ? Order.ASC : Order.DESC;
        return getByName(order.getProperty())
                .map(querydslOrder -> new OrderSpecifier<>(direction, querydslOrder.getComparableExpressionBase()));
    }
}
