package org.chzz.market.domain;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import static org.chzz.market.domain.QTest.test;


@RequiredArgsConstructor
public class TestRepositoryCustomImpl implements TestRepositoryCustom{
    private final JPAQueryFactory queryFactory;
    @Override
    public String findVal(Test testEntity) {
        return queryFactory
                .select(test.value)
                .from(test)
                .where(test.eq(testEntity))
                .fetchOne();
    }
}
