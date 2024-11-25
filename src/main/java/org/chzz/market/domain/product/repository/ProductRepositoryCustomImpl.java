package org.chzz.market.domain.product.repository;

import static org.chzz.market.common.util.QuerydslUtil.nullSafeBuilder;
import static org.chzz.market.common.util.QuerydslUtil.nullSafeBuilderIgnore;
import static org.chzz.market.domain.auction.entity.QAuction.auction;
import static org.chzz.market.domain.image.entity.QImage.image;
import static org.chzz.market.domain.like.entity.QLike.like;
import static org.chzz.market.domain.product.entity.Product.Category;
import static org.chzz.market.domain.product.entity.QProduct.product;
import static org.chzz.market.domain.user.entity.QUser.user;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.chzz.market.common.util.QuerydslOrder;
import org.chzz.market.common.util.QuerydslOrderProvider;
import org.chzz.market.domain.image.dto.ImageResponse;
import org.chzz.market.domain.image.dto.QImageResponse;
import org.chzz.market.domain.product.dto.ProductDetailsResponse;
import org.chzz.market.domain.product.dto.ProductResponse;
import org.chzz.market.domain.product.dto.QProductDetailsResponse;
import org.chzz.market.domain.product.dto.QProductResponse;
import org.chzz.market.domain.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

@RequiredArgsConstructor
public class ProductRepositoryCustomImpl implements ProductRepositoryCustom {
    private final JPAQueryFactory jpaQueryFactory;
    private final QuerydslOrderProvider querydslOrderProvider;

    /**
     * 사전 등록 상품 리스트를 조회합니다.
     *
     * @param category 카테고리
     * @param userId   사용자 ID
     * @param pageable 페이징 정보
     * @return 페이징된 사전 등록 상품 리스트
     */
    @Override
    public Page<ProductResponse> findProductsByCategory(Category category, Long userId, Pageable pageable) {
        JPAQuery<?> baseQuery = jpaQueryFactory.from(product)
                .leftJoin(auction).on(auction.product.id.eq(product.id))
                .where(auction.id.isNull().and(categoryEqIgnoreNull(category)));

        List<ProductResponse> content = baseQuery
                .select(new QProductResponse(
                        product.id,
                        product.name,
                        image.cdnPath,
                        product.minPrice,
                        product.likes.size().longValue(),
                        isProductLikedByUser(userId),
                        userIdEq(userId)
                ))
                .leftJoin(image).on(image.product.eq(product).and(isRepresentativeImage()))
                .join(product.user, user)
                .groupBy(product.id, product.name, image.cdnPath, product.minPrice)
                .orderBy(querydslOrderProvider.getOrderSpecifiers(pageable))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = baseQuery.select(product.countDistinct());

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    /**
     * 사용자 ID와 상품 ID에 따라 사전 등록 상품 상세 정보를 조회합니다.
     *
     * @param productId 상품 ID
     * @param userId    사용자 ID
     * @return 상품 상세 정보
     */
    @Override
    public Optional<ProductDetailsResponse> findProductDetailsById(Long productId, Long userId) {

        Optional<ProductDetailsResponse> result = Optional.ofNullable(jpaQueryFactory
                .select(new QProductDetailsResponse(
                        product.id,
                        product.name,
                        user.nickname,
                        user.profileImageUrl,
                        product.minPrice,
                        product.updatedAt,
                        product.description,
                        product.likes.size().longValue(),
                        isProductLikedByUser(userId),
                        userIdEq(userId),
                        product.category
                ))
                .from(product)
                .leftJoin(auction).on(auction.product.id.eq(product.id))
                .join(product.user, user)
                .where(auction.id.isNull().and(product.id.eq(productId)))
                .fetchOne());

        // 이미지 목록 추가
        result.ifPresent(response -> response.addImageList(getImagesByProductId(productId)));
        return result;
    }

    /**
     * 사용자 닉네임에 따라 사용자가 등록한 사전 등록 상품 리스트를 조회합니다.
     *
     * @param nickname 사용자 닉네임
     * @param pageable 페이징 정보
     * @return 페이징된 사전 등록 상품 리스트
     */
    @Override
    public Page<ProductResponse> findProductsByNickname(String nickname, Pageable pageable) {

        JPAQuery<?> baseQuery = jpaQueryFactory.from(product)
                .join(product.user, user)
                .leftJoin(auction).on(auction.product.eq(product))
                .leftJoin(like).on(like.product.eq(product).and(like.user.nickname.eq(nickname)))
                .where(auction.isNull().and(user.nickname.eq(nickname)));

        return getProductResponses(pageable, baseQuery);
    }

    @Override
    public Page<ProductResponse> findProductsByUserId(Long userId, Pageable pageable) {
        JPAQuery<?> baseQuery = jpaQueryFactory.from(product)
                .join(product.user, user)
                .leftJoin(auction).on(auction.product.eq(product))
                .leftJoin(like).on(like.product.eq(product).and(like.user.id.eq(userId)))
                .where(auction.isNull().and(user.id.eq(userId)));

        return getProductResponses(pageable, baseQuery);
    }

    private Page<ProductResponse> getProductResponses(Pageable pageable, JPAQuery<?> baseQuery) {
        List<ProductResponse> content = baseQuery
                .select(new QProductResponse(
                        product.id,
                        product.name,
                        image.cdnPath,
                        product.minPrice,
                        product.likes.size().longValue(),
                        like.isNotNull()
                ))
                .leftJoin(image).on(image.product.eq(product).and(isRepresentativeImage()))
                .orderBy(querydslOrderProvider.getOrderSpecifiers(pageable))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = baseQuery
                .select(product.count());

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    /**
     * 사용자 ID에 따라 사용자가 참여한 사전 경매 리스트를 조회합니다.
     *
     * @param userId   사용자 ID
     * @param pageable 페이징 정보
     * @return 페이징된 사전 경매 리스트
     */
    @Override
    public Page<ProductResponse> findLikedProductsByUserId(Long userId, Pageable pageable) {

        JPAQuery<?> baseQuery = jpaQueryFactory.from(product)
                .join(product.likes, like)
                .join(like.user, user)
                .leftJoin(auction).on(auction.product.eq(product))
                .where(user.id.eq(userId).and(auction.isNull()));

        List<ProductResponse> content = baseQuery
                .select(new QProductResponse(
                        product.id,
                        product.name,
                        image.cdnPath,
                        product.minPrice,
                        product.likes.size().longValue()
                ))
                .leftJoin(image).on(image.product.eq(product).and(isRepresentativeImage()))
                .orderBy(querydslOrderProvider.getOrderSpecifiers(pageable))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = baseQuery
                .select(like.count());

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    @Override
    public Optional<Product> findProductByIdWithImage(Long productId) {
        return Optional.ofNullable(jpaQueryFactory.selectFrom(product)
                .leftJoin(product.images, image)
                .fetchJoin()
                .where(product.id.eq(productId))
                .fetchOne());
    }

    /**
     * 제품의 이미지 리스트를 조회
     */
    private List<ImageResponse> getImagesByProductId(Long productId) {
        return jpaQueryFactory
                .select(new QImageResponse(image.id, image.cdnPath))
                .from(image)
                .where(image.product.id.eq(productId))
                .orderBy(image.sequence.asc())
                .fetch();
    }

    /**
     * 상품의 대표 이미지를 조회하기 위한 조건을 반환합니다.
     *
     * @return 대표 이미지(첫 번째 이미지)의 sequence가 1인 조건식
     */
    private BooleanExpression isRepresentativeImage() {
        return image.sequence.eq(1);
    }

    /**
     * 사용자가 특정 상품을 좋아요(Like)했는지 여부를 확인합니다.
     *
     * @param userId 사용자 ID
     * @return 사용자가 해당 상품을 좋아요한 경우 true, 그렇지 않으면 false
     */
    private BooleanExpression isProductLikedByUser(Long userId) {
        return JPAExpressions.selectOne()
                .from(like)
                .where(like.product.eq(product)
                        .and(likeUserIdEq(userId)))
                .exists();
    }

    private BooleanBuilder likeUserIdEq(Long userId) {
        return nullSafeBuilder(() -> like.user.id.eq(userId));
    }

    private BooleanBuilder categoryEqIgnoreNull(Category category) {
        return nullSafeBuilderIgnore(() -> product.category.eq(category));
    }

    private BooleanBuilder userIdEq(Long userId) {
        return nullSafeBuilder(() -> user.id.eq(userId));
    }

    @Getter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public enum ProductOrder implements QuerydslOrder {
        POPULARITY("most-liked", product.likes.size().desc()),
        NEWEST("product-newest", product.createdAt.desc());

        private final String name;
        private final OrderSpecifier<?> orderSpecifier;
    }

}
