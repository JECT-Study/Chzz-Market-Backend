package org.chzz.market.common.util;

import com.querydsl.core.types.OrderSpecifier;

public interface QuerydslOrder {
    String getName();

    OrderSpecifier<?> getOrderSpecifier();
}
