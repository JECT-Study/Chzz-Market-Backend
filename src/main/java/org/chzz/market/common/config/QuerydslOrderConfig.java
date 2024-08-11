package org.chzz.market.common.config;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.chzz.market.common.util.QuerydslOrder;
import org.chzz.market.common.util.QuerydslOrderProvider;
import org.chzz.market.common.util.QuerydslOrderRegistry;
import org.reflections.Reflections;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.support.GenericApplicationContext;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class QuerydslOrderConfig {
    private final GenericApplicationContext applicationContext;

    @Bean
    @Lazy(false)
    public QuerydslOrderRegistry querydslOrderRegistry() {
        Reflections reflections = new Reflections("org.chzz.market.domain");
        Set<Class<? extends QuerydslOrder>> enumClasses = reflections.getSubTypesOf(QuerydslOrder.class);

        List<QuerydslOrder> registeredBeans = enumClasses.stream()
                .filter(Class::isEnum)
                .flatMap(enumClass -> {
                    QuerydslOrder[] constants = enumClass.getEnumConstants();
                    for (QuerydslOrder constant : constants) {
                        String beanName = enumClass.getSimpleName() + "_" + constant.getName();
                        applicationContext.registerBean(beanName, QuerydslOrder.class, () -> constant);
                    }
                    return List.of(constants).stream();
                })
                .collect(Collectors.toList());

        registeredBeans.forEach(querydslOrder ->
                log.info("Registered sort type of {}: {}.{}", querydslOrder.getClass(), querydslOrder.getName(), querydslOrder.getComparableExpressionBase())
        );

        return new QuerydslOrderRegistry(registeredBeans);
    }

    @Bean
    public QuerydslOrderProvider querydslOrderProvider() {
        return new QuerydslOrderProvider(querydslOrderRegistry());
    }
}