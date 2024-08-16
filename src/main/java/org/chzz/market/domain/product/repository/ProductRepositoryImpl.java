package org.chzz.market.domain.product.repository;

import com.querydsl.core.types.OrderSpecifier;
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
import org.chzz.market.domain.auction.entity.QAuction;
import org.chzz.market.domain.image.entity.QImage;
import org.chzz.market.domain.like.entity.QLike;
import org.chzz.market.domain.product.dto.*;
import org.chzz.market.domain.product.entity.QProduct;
import org.chzz.market.domain.user.entity.QUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

import java.util.List;
import java.util.Optional;

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
                .leftJoin(auction).on(auction.product.id.eq(product.id))
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

        return PageableExecutionUtils.getPage(content, pageable, () ->
            Optional.ofNullable(jpaQueryFactory
                    .select(product.countDistinct())
                    .from(product)
                    .leftJoin(auction).on(auction.product.id.eq(product.id))
                    .where(product.category.eq(category)
                            .and(auction.id.isNull()))
                    .fetchOne())
            .orElse(0L)
        );
    }

    /**
     * 사용자 ID와 상품 ID에 따라 사전 등록 상품 상세 정보를 조회합니다.
     * @param productId 상품 ID
     * @param userId    사용자 ID
     * @return
     */
    @Override
    public Optional<ProductDetailsResponse> findProductDetailsById(Long productId, Long userId) {
        QProduct product = QProduct.product;
        QUser seller = QUser.user;
        QLike like = QLike.like;
        QImage image = QImage.image;

        ProductDetailsResponse result = jpaQueryFactory
                .select(new QProductDetailsResponse(
                        product.id,
                        product.name,
                        seller.nickname,
                        product.minPrice,
                        product.createdAt,
                        product.description,
                        JPAExpressions.select(like.count())
                                .from(like)
                                .where(like.product.eq(product)),
                        JPAExpressions.selectOne()
                                .from(like)
                                .where(like.product.eq(product).and(like.user.id.eq(userId)))
                                .exists()
                ))
                .from(product)
                .join(product.user, seller)
                .where(product.id.eq(productId))
                .fetchOne();

        if (result != null) {
            List<String> imageUrls = jpaQueryFactory
                    .select(image.cdnPath)
                    .from(image)
                    .where(image.product.id.eq(productId))
                    .orderBy(image.id.asc())
                    .fetch();
            result.addImageList(imageUrls);
        }

        return Optional.ofNullable(result);
    }

    /**
     * 사용자 ID에 따라 사용자가 등록한 사전 등록 상품 리스트를 조회합니다.
     * @param userId 사용자 ID
     * @param pageable 페이징 정보
     * @return 페이징된 사전 등록 상품 리스트
     */
    @Override
    public Page<ProductResponse> findMyProductsByUserId(Long userId, Pageable pageable) {
        QProduct product = QProduct.product;
        QImage image = QImage.image;
        QAuction auction = QAuction.auction;
        QUser user = QUser.user;

        JPAQuery<?> baseQuery = jpaQueryFactory.from(product)
                .join(product.user, user)
                .leftJoin(auction).on(auction.product.eq(product))
                .where(auction.id.isNull().and(user.id.eq(userId)));

        List<ProductResponse> content = baseQuery
                .select(new QProductResponse(
                        product.id,
                        product.name,
                        image.cdnPath,
                        product.minPrice,
                        getLikeCount(),
                        JPAExpressions.selectOne()
                                .from(like)
                                .where(like.product.eq(product)
                                        .and(like.user.id.eq(userId)))
                                .exists()
                ))
                .leftJoin(image).on(image.product.id.eq(product.id)
                        .and(image.id.eq(getFirstImageId())))
                .orderBy(querydslOrderProvider.getOrderSpecifiers(pageable))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = baseQuery
                .select(product.countDistinct())
                .groupBy(product.id);

        return PageableExecutionUtils.getPage(content, pageable, () -> {
            Long totalCount = countQuery.fetchOne();
            return totalCount != null ? totalCount : 0L;
        });

    }

    private JPQLQuery<Long> getFirstImageId() {
        QImage imageSub = new QImage("imageSub");
        return JPAExpressions.select(imageSub.id.min())
                .from(imageSub)
                .where(imageSub.product.id.eq(product.id));
    }

    private JPQLQuery<Long> getLikeCount() {
        return JPAExpressions.select(like.count())
                .from(QLike.like)
                .where(QLike.like.product.eq(QProduct.product));
    }

    @Getter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public enum ProductOrder implements QuerydslOrder {
        POPULARITY("product_popularity", product.likes.size().desc()),
        EXPENSIVE("product_expensive", product.minPrice.desc()),
        CHEAP("product_cheap", product.minPrice.asc()),
        NEWEST("product_newest", product.createdAt.desc());

        private final String name;
        private final OrderSpecifier<?> orderSpecifier;
    }

}
