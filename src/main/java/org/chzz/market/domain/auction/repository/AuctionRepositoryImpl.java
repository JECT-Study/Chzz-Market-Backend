package org.chzz.market.domain.auction.repository;

import static org.chzz.market.domain.auction.entity.Auction.AuctionStatus.*;
import static org.chzz.market.domain.auction.entity.QAuction.auction;
import static org.chzz.market.domain.bid.entity.QBid.bid;
import static org.chzz.market.domain.image.entity.QImage.image;
import static org.chzz.market.domain.product.entity.QProduct.product;
import static org.chzz.market.domain.user.entity.QUser.user;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.chzz.market.domain.auction.dto.response.AuctionDetailsResponse;
import org.chzz.market.common.util.QuerydslOrder;
import org.chzz.market.common.util.QuerydslOrderProvider;
import org.chzz.market.domain.auction.dto.response.AuctionResponse;
import org.chzz.market.domain.auction.dto.response.MyAuctionResponse;
import org.chzz.market.domain.auction.dto.response.QAuctionDetailsResponse;
import org.chzz.market.domain.auction.dto.response.QAuctionResponse;
import org.chzz.market.domain.auction.dto.response.QMyAuctionResponse;
import org.chzz.market.domain.image.entity.QImage;
import org.chzz.market.domain.product.entity.Product.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

@RequiredArgsConstructor
public class AuctionRepositoryImpl implements AuctionRepositoryCustom {
    private final JPAQueryFactory jpaQueryFactory;
    private final QuerydslOrderProvider querydslOrderProvider;

    /**
     * 카테고리와 정렬 조건에 따라 경매 리스트를 조회합니다.
     *
     * @param category 카테고리
     * @param userId   사용자 ID
     * @param pageable 페이징 정보
     * @return 페이징된 경매 응답 리스트
     */
    @Override
    public Page<AuctionResponse> findAuctionsByCategory(Category category, Long userId,
                                                        Pageable pageable) {
        JPAQuery<?> baseQuery = jpaQueryFactory.from(auction)
                .join(auction.product, product)
                .leftJoin(bid).on(bid.auction.id.eq(auction.id))
                .where(auction.product.category.eq(category))
                .where(auction.status.eq(PROCEEDING));

        List<AuctionResponse> content = baseQuery
                .select(new QAuctionResponse(
                        auction.id,
                        product.name,
                        image.cdnPath,
                        timeRemaining().longValue(),
                        product.minPrice.longValue(),
                        bid.countDistinct(),
                        isParticipating(userId)
                ))
                .leftJoin(image).on(image.product.id.eq(product.id).and(image.id.eq(getFirstImageId())))
                .groupBy(auction.id, product.name, image.cdnPath, auction.createdAt, product.minPrice)
                .orderBy(querydslOrderProvider.getOrderSpecifiers(pageable))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = baseQuery
                .select(auction.count());
        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchCount);
    }

    /**
     * 경매 ID와 사용자 ID로 경매 상세 정보를 조회합니다.
     *
     * @param auctionId 경매 ID
     * @param userId    사용자 ID
     * @return 경매 상세 응답
     */
    @Override
    public Optional<AuctionDetailsResponse> findAuctionDetailsById(Long auctionId, Long userId) {
        Optional<AuctionDetailsResponse> auctionDetailsResponse = Optional.ofNullable(jpaQueryFactory
                .select(new QAuctionDetailsResponse(
                        product.id,
                        user.id,
                        user.nickname,
                        product.name,
                        product.description,
                        product.minPrice,
                        auction.endDateTime,
                        auction.status,
                        user.id.eq(userId),
                        getBidCount(),
                        bid.id.isNotNull(),
                        bid.id,
                        bid.amount.coalesce(0L),
                        bid.count.coalesce(3)
                ))
                .from(auction)
                .join(auction.product, product)
                .join(product.user, user)
                .leftJoin(bid).on(bid.auction.id.eq(auctionId).and(bid.bidder.id.eq(userId)))
                .where(
                        auction.id.eq(auctionId)
                                .and(auction.status.eq(PROCEEDING).or(auction.status.eq(ENDED)))
                )
                .fetchOne());

        auctionDetailsResponse.ifPresent(response -> response.addImageList(getImageList(response.getProductId())));

        return auctionDetailsResponse;
    }

    @Override
    public Page<MyAuctionResponse> findAuctionsByUserId(Long userId, Pageable pageable) {
        JPAQuery<?> baseQuery = jpaQueryFactory.from(auction)
                .join(auction.product, product)
                .join(product.user, user)
                .where(auction.status.ne(CANCELLED)
                        .and(user.id.eq(userId)));

        List<MyAuctionResponse> content = baseQuery
                .select(new QMyAuctionResponse(
                        auction.id,
                        product.name,
                        image.cdnPath,
                        timeRemaining().longValue(),
                        product.minPrice.longValue(),
                        getBidCount(),
                        auction.status,
                        auction.createdAt))
                .leftJoin(image).on(image.product.id.eq(product.id)
                        .and(image.id.eq(getFirstImageId())))
                .orderBy(querydslOrderProvider.getOrderSpecifiers(pageable))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = baseQuery
                .select(auction.count());

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchCount);
    }

    /**
     * 상품의 첫 번째 이미지를 조회합니다.
     *
     * @return 첫 번째 이미지 ID
     */
    private JPQLQuery<Long> getFirstImageId() {
        QImage imageSub = new QImage("imageSub");
        return JPAExpressions.select(imageSub.id.min())
                .from(imageSub)
                .where(imageSub.product.id.eq(product.id));
    }

    /**
     * 사용자가 참여 중인 경매인지 확인합니다.
     *
     * @param userId 사용자 ID
     * @return 사용자가 참여 중인 경우 true, 그렇지 않으면 false
     */
    private BooleanExpression isParticipating(Long userId) {
        return JPAExpressions.selectFrom(bid)
                .where(bid.auction.id.eq(auction.id).and(bid.bidder.id.eq(userId)))
                .exists();
    }

    /**
     * 경매 참여자 수를 조회합니다.
     *
     * @return 참여자 수
     */
    private JPQLQuery<Long> getBidCount() {
        return JPAExpressions
                .select(bid.count())
                .from(bid)
                .where(bid.auction.id.eq(auction.id));
    }

    /**
     * 상품의 이미지 리스트를 조회합니다.
     *
     * @param productId 상품 ID
     * @return 이미지 경로 리스트
     */
    private List<String> getImageList(Long productId) {
        return jpaQueryFactory.select(image.cdnPath)
                .from(image)
                .where(image.product.id.eq(productId))
                .fetch();
    }

    private static NumberExpression<Integer> timeRemaining() {
        return Expressions.numberTemplate(Integer.class, "TIMESTAMPDIFF(SECOND, CURRENT_TIMESTAMP, {0})",
                auction.endDateTime);
    }

    @Getter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public enum AuctionOrder implements QuerydslOrder {
        POPULARITY("popularity", auction.bids.size().desc()),
        EXPENSIVE("expensive", product.minPrice.desc()),
        CHEAP("cheap", product.minPrice.asc()),
        NEWEST("newest", auction.createdAt.desc());

        private final String name;
        private final OrderSpecifier<?> orderSpecifier;
    }
}
