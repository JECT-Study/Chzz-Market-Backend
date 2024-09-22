package org.chzz.market.common.util;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import java.util.function.Supplier;

public class QuerydslUtil {
    /**
     * Querydsl eq() 메서드에 null 값이 전달되면, 해당 조건은 false로 평가
     */
    public static BooleanBuilder nullSafeBuilder(Supplier<BooleanExpression> f) {
        try {
            return new BooleanBuilder(f.get());
        } catch (IllegalArgumentException e) {
            return new BooleanBuilder(Expressions.FALSE);
        }
    }

    /**
     * Querydsl eq() 메서드에 null 값이 전달되면, 해당 조건은 무시
     */
    public static BooleanBuilder nullSafeBuilderIgnore(Supplier<BooleanExpression> f) {
        try {
            BooleanExpression expression = f.get();
            return expression != null ? new BooleanBuilder(expression) : new BooleanBuilder();
        } catch (IllegalArgumentException e) {
            return new BooleanBuilder();
        }
    }
}
