package org.chzz.market.common.util;

import com.querydsl.core.types.dsl.ComparableExpressionBase;

public interface QuerydslOrder {
    String getName();

    ComparableExpressionBase<?> getComparableExpressionBase();
}