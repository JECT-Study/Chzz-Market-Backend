package org.chzz.market.common.util;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.chzz.market.common.error.GlobalErrorCode;
import org.chzz.market.common.error.GlobalException;

public class QuerydslOrderRegistry {

    private final Map<String, QuerydslOrder> querydslOrderMap;

    public QuerydslOrderRegistry(List<QuerydslOrder> querydslOrders) {
        this.querydslOrderMap = querydslOrders.stream()
                .collect(Collectors.toConcurrentMap(QuerydslOrder::getName, order -> order));
    }

    public QuerydslOrder getOrderByName(String name) throws GlobalException{
        try{
            return querydslOrderMap.get(name);
        }catch (NullPointerException e){
            throw new GlobalException(GlobalErrorCode.UNSUPPORTED_SORT_TYPE);
        }
    }

    public boolean isValidOrderProperty(String property) {
        return querydslOrderMap.containsKey(property);
    }
}