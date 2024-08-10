package org.chzz.market.common.util;

import com.querydsl.core.types.OrderSpecifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class QuerydslOrderProvider {
    private final QuerydslOrderRegistry querydslOrderRegistry;
    public Optional<QuerydslOrder> getByName(String name) {
        return Optional.of(querydslOrderRegistry.getOrderByName(name));
    }


    public OrderSpecifier[] getOrderSpecifiers(Pageable pageable) {
        List<OrderSpecifier<?>> orderSpecifiers = new ArrayList<>();
        if (!pageable.getSort().isEmpty()) {
            for (Order order : pageable.getSort()) {
                com.querydsl.core.types.Order direction =
                        order.getDirection().isAscending()
                                ? com.querydsl.core.types.Order.ASC
                                : com.querydsl.core.types.Order.DESC;

                getByName(order.getProperty()).ifPresent(querydslOrder ->
                        orderSpecifiers
                                .add(new OrderSpecifier<>(direction, querydslOrder.getComparableExpressionBase())));
            }
        } else {
            orderSpecifiers.add(OrderByNull.DEFAULT);
        }
        return orderSpecifiers.toArray(OrderSpecifier[]::new);
    }
}
