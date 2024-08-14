package org.chzz.market.domain.product.repository;

import com.querydsl.core.types.dsl.ComparableExpressionBase;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.chzz.market.common.util.QuerydslOrder;
import org.chzz.market.common.util.QuerydslOrderProvider;
import org.chzz.market.domain.image.entity.QImage;
import org.chzz.market.domain.product.dto.ProductResponse;
import org.chzz.market.domain.product.dto.QProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

import java.util.List;

import static org.chzz.market.domain.auction.entity.QAuction.auction;
import static org.chzz.market.domain.image.entity.QImage.image;
import static org.chzz.market.domain.like.entity.QLike.like;
import static org.chzz.market.domain.product.entity.Product.*;
import static org.chzz.market.domain.product.entity.QProduct.product;

@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepositoryCustom {
    private final JPAQueryFactory jpaQueryFactory;
    private final QuerydslOrderProvider querydslOrderProvider;

    /**
     * 카테고리와 정렬 조건에 따라 사전 등록 상품 리스트를 조회합니다.
     * @param category 카테고리
     * @param userId   사용자 ID
     * @param pageable 페이징 정보
     * @return 페이징된 사전 등록 상품 리스트
     */
    @Override
    public Page<ProductResponse> findProductsByCategory(Category category, Long userId, Pageable pageable) {

        JPAQuery<?> baseQuery = jpaQueryFactory.from(product)
                .leftJoin(auction).on(auction.product.eq(product))
                .where(auction.id.isNull())
                .where(product.category.eq(category));

        List<ProductResponse> content = baseQuery
                .select(new QProductResponse(
                        product.id,
                        product.name,
                        image.cdnPath,
                        product.minPrice,
                        product.likes.size().longValue(),
                        JPAExpressions.selectOne()
                                .from(like)
                                .where(like.product.eq(product)
                                .and(like.user.id.eq(userId)))
                                .exists()
                ))
                .leftJoin(image).on(image.product.id.eq(product.id).and(image.id.eq(getFirstImageId())))
                .groupBy(product.id, product.name, image.cdnPath, product.minPrice)
                .orderBy(querydslOrderProvider.getOrderSpecifiers(pageable))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        return PageableExecutionUtils.getPage(content, pageable, () -> {
            JPAQuery<Long> countQuery = jpaQueryFactory
                    .select(product.countDistinct())
                    .from(product)
                    .leftJoin(auction).on(auction.product.id.eq(product.id))
                    .where(product.category.eq(category)
                            .and(auction.id.isNull()));
            return countQuery.fetchOne() != null ? countQuery.fetchOne() : 0L;
        });
    }

    private JPQLQuery<Long> getFirstImageId() {
        QImage imageSub = new QImage("imageSub");
        return JPAExpressions.select(imageSub.id.min())
                .from(imageSub)
                .where(imageSub.product.id.eq(product.id));
    }

    @Getter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public enum ProductOrder implements QuerydslOrder {
        POPULARITY("product_popularity", product.likes.size().multiply(-1)),
        EXPENSIVE("product_expensive", product.minPrice.multiply(-1)),
        CHEAP("product_cheap", product.minPrice),
        NEWEST("product_newest", product.createdAt.count().multiply(-1));

        private final String name;
        private final ComparableExpressionBase<?> comparableExpressionBase;
    }

}
