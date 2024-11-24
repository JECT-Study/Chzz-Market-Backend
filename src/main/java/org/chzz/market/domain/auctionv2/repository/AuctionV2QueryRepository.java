package org.chzz.market.domain.auctionv2.repository;

import static com.querydsl.core.types.dsl.Expressions.numberTemplate;
import static org.chzz.market.common.util.QuerydslUtil.nullSafeBuilder;
import static org.chzz.market.common.util.QuerydslUtil.nullSafeBuilderIgnore;
import static org.chzz.market.domain.auctionv2.entity.AuctionStatus.PRE;
import static org.chzz.market.domain.auctionv2.entity.QAuctionV2.auctionV2;
import static org.chzz.market.domain.bid.entity.Bid.BidStatus.ACTIVE;
import static org.chzz.market.domain.bid.entity.Bid.BidStatus.CANCELLED;
import static org.chzz.market.domain.bid.entity.QBid.bid;
import static org.chzz.market.domain.image.entity.QImageV2.imageV2;
import static org.chzz.market.domain.likev2.entity.QLikeV2.likeV2;
import static org.chzz.market.domain.orderv2.entity.QOrderV2.orderV2;
import static org.chzz.market.domain.user.entity.QUser.user;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
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
import org.chzz.market.domain.auctionv2.dto.response.OfficialAuctionDetailResponse;
import org.chzz.market.domain.auctionv2.dto.response.OfficialAuctionResponse;
import org.chzz.market.domain.auctionv2.dto.response.PreAuctionDetailResponse;
import org.chzz.market.domain.auctionv2.dto.response.PreAuctionResponse;
import org.chzz.market.domain.auctionv2.dto.response.QWonAuctionDetailsResponse;
import org.chzz.market.domain.auctionv2.dto.response.WonAuctionDetailsResponse;
import org.chzz.market.domain.auctionv2.entity.AuctionStatus;
import org.chzz.market.domain.auctionv2.entity.Category;
import org.chzz.market.domain.bid.entity.QBid;
import org.chzz.market.domain.image.dto.ImageResponse;
import org.chzz.market.domain.image.dto.QImageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AuctionV2QueryRepository {
    private final JPAQueryFactory jpaQueryFactory;
    private final QuerydslOrderProvider querydslOrderProvider;

    /**
     * 낙찰자 정보 조회
     */
    public Optional<WonAuctionDetailsResponse> findWinningBidById(Long auctionId) {
        return Optional.ofNullable(jpaQueryFactory.select(
                        new QWonAuctionDetailsResponse(auctionV2.id, auctionV2.name, imageV2.cdnPath, bid.amount))
                .from(auctionV2)
                .leftJoin(bid).on(bid.bidderId.eq(auctionV2.winnerId)
                        .and(bid.auctionId.eq(auctionV2.id)))
                .leftJoin(auctionV2.images, imageV2).on(imageV2.sequence.eq(1))
                .where(auctionV2.id.eq(auctionId))
                .fetchOne());
    }

    /**
     * 사전 경매 상세 조회
     */
    public Optional<PreAuctionDetailResponse> findPreAuctionDetailById(Long userId, Long auctionId) {
        Optional<PreAuctionDetailResponse> result = Optional.ofNullable(jpaQueryFactory
                .select(
                        Projections.constructor(
                                PreAuctionDetailResponse.class,
                                auctionV2.id,
                                user.nickname,
                                user.profileImageUrl,
                                auctionV2.name,
                                auctionV2.description,
                                auctionV2.minPrice,
                                userIdEq(userId),
                                auctionV2.status,
                                auctionV2.category,
                                auctionV2.updatedAt,
                                auctionV2.likeCount,
                                likeV2.id.isNotNull()
                        )
                )
                .from(auctionV2)
                .join(auctionV2.seller, user)
                .leftJoin(likeV2).on(likeV2.auctionId.eq(auctionV2.id).and(likeUserIdEq(userId)))
                .where(auctionV2.id.eq(auctionId))
                .fetchOne());

        result.ifPresent(response -> response.addImageList(getImagesByAuctionId(response.getAuctionId())));
        return result;
    }

    /**
     * 정식 경매 상세 조회
     */
    public Optional<OfficialAuctionDetailResponse> findOfficialAuctionDetailById(Long userId, Long auctionId) {
        QBid activeBid = new QBid("bidActive");
        QBid canceledBid = new QBid("bidCanceled");
        Optional<OfficialAuctionDetailResponse> officialAuctionDetailResponse = Optional.ofNullable(jpaQueryFactory
                .select(
                        Projections.constructor(
                                OfficialAuctionDetailResponse.class,
                                auctionV2.id,
                                user.nickname,
                                user.profileImageUrl,
                                auctionV2.name,
                                auctionV2.description,
                                auctionV2.minPrice,
                                userIdEq(userId),
                                auctionV2.status,
                                auctionV2.category,
                                timeRemaining().longValue(),
                                auctionV2.bidCount,
                                activeBid.id.isNotNull(),
                                activeBid.id,
                                activeBid.amount.coalesce(0L),
                                activeBid.count.coalesce(3),
                                canceledBid.id.isNotNull(),
                                winnerIdEq(userId),
                                auctionV2.winnerId.isNotNull(),
                                orderV2.isNotNull()
                        )
                )
                .from(auctionV2)
                .join(auctionV2.seller, user)
                .leftJoin(activeBid).on(activeBid.auctionId.eq(auctionId) // 활성화된 입찰 조인
                        .and(activeBid.status.eq(ACTIVE))
                        .and(bidderIdEqSub(activeBid, userId)))
                .leftJoin(canceledBid).on(canceledBid.auctionId.eq(auctionId) // 취소된 입찰 조인
                        .and(canceledBid.status.eq(CANCELLED))
                        .and(bidderIdEqSub(canceledBid, userId)))
                .leftJoin(orderV2).on(orderV2.auction.eq(auctionV2))
                .where(auctionV2.id.eq(auctionId))
                .fetchOne());

        officialAuctionDetailResponse.ifPresent(
                response -> response.addImageList(getImagesByAuctionId(response.getAuctionId())));

        return officialAuctionDetailResponse;
    }

    /**
     * 사전 경매 목록 조회
     */
    public Page<PreAuctionResponse> findPreAuctions(Long userId, Category category, Pageable pageable) {
        JPAQuery<?> baseQuery = jpaQueryFactory.from(auctionV2)
                .where(categoryEqIgnoreNull(category).and(auctionV2.status.eq(PRE)));

        List<PreAuctionResponse> content = baseQuery
                .select(
                        Projections.constructor(
                                PreAuctionResponse.class,
                                auctionV2.id,
                                auctionV2.name,
                                imageV2.cdnPath,
                                auctionV2.minPrice.longValue(),
                                userIdEq(userId),
                                auctionV2.likeCount,
                                likeV2.id.isNotNull()
                        )
                )
                .join(auctionV2.seller, user)
                .leftJoin(auctionV2.images, imageV2).on(imageV2.sequence.eq(1))
                .leftJoin(likeV2).on(likeV2.auctionId.eq(auctionV2.id).and(likeUserIdEq(userId)))
                .orderBy(querydslOrderProvider.getOrderSpecifiers(pageable))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = baseQuery.select(auctionV2.count());

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    /**
     * 정식 경매 목록 조회
     */
    public Page<OfficialAuctionResponse> findOfficialAuctions(Long userId, Category category, AuctionStatus status,
                                                              Integer endWithinSeconds,
                                                              Pageable pageable) {
        JPAQuery<?> baseQuery = jpaQueryFactory.from(auctionV2)
                .where(categoryEqIgnoreNull(category).and(auctionV2.status.eq(status))
                        .and(timeRemainingIgnoreNull(endWithinSeconds)));

        List<OfficialAuctionResponse> content = baseQuery
                .select(
                        Projections.constructor(
                                OfficialAuctionResponse.class,
                                auctionV2.id,
                                auctionV2.name,
                                imageV2.cdnPath,
                                auctionV2.minPrice.longValue(),
                                userIdEq(userId),
                                timeRemaining().longValue(),
                                auctionV2.bidCount,
                                bid.id.isNotNull()
                        )
                )
                .join(auctionV2.seller, user)
                .leftJoin(bid).on(bid.auctionId.eq(auctionV2.id).and(bidderIdEq(userId)).and(bid.status.eq(ACTIVE)))
                .leftJoin(auctionV2.images, imageV2).on(imageV2.sequence.eq(1))
                .orderBy(querydslOrderProvider.getOrderSpecifiers(pageable))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = baseQuery.select(auctionV2.count());

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    /**
     * 사용자가 등록한 사전경매 목록 조회
     */
    public Page<PreAuctionResponse> findPreAuctionsByUserId(Long userId, Pageable pageable) {
        JPAQuery<?> baseQuery = jpaQueryFactory.from(auctionV2)
                .join(auctionV2.seller, user).on(user.id.eq(userId))
                .where(auctionV2.status.eq(PRE));

        List<PreAuctionResponse> content = baseQuery
                .select(
                        Projections.constructor(
                                PreAuctionResponse.class,
                                auctionV2.id,
                                auctionV2.name,
                                imageV2.cdnPath,
                                auctionV2.minPrice.longValue(),
                                Expressions.TRUE,
                                auctionV2.likeCount,
                                Expressions.FALSE
                        )
                )
                .leftJoin(auctionV2.images, imageV2).on(imageV2.sequence.eq(1))
                .orderBy(querydslOrderProvider.getOrderSpecifiers(pageable))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = baseQuery.select(auctionV2.count());

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    /**
     * 사용자가 좋아요한 사전 경매목록 조회
     */
    public Page<PreAuctionResponse> findLikedAuctionsByUserId(Long userId, Pageable pageable) {
        JPAQuery<?> baseQuery = jpaQueryFactory.from(auctionV2)
                .join(likeV2).on(likeV2.auctionId.eq(auctionV2.id).and(likeV2.userId.eq(userId)))
                .where(auctionV2.status.eq(PRE));

        List<PreAuctionResponse> content = baseQuery
                .select(
                        Projections.constructor(
                                PreAuctionResponse.class,
                                auctionV2.id,
                                auctionV2.name,
                                imageV2.cdnPath,
                                auctionV2.minPrice.longValue(),
                                userIdEq(userId),
                                auctionV2.likeCount,
                                likeV2.id.isNotNull()
                        )
                )
                .join(auctionV2.seller, user)
                .leftJoin(auctionV2.images, imageV2).on(imageV2.sequence.eq(1))
                .orderBy(querydslOrderProvider.getOrderSpecifiers(pageable))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = baseQuery.select(auctionV2.count());

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    private List<ImageResponse> getImagesByAuctionId(Long auctionId) {
        return jpaQueryFactory
                .select(new QImageResponse(imageV2.id, imageV2.cdnPath))
                .from(imageV2)
                .where(imageV2.auction.id.eq(auctionId))
                .orderBy(imageV2.sequence.asc())
                .fetch();
    }

    private BooleanBuilder userIdEq(Long userId) {
        return nullSafeBuilder(() -> user.id.eq(userId));
    }

    private BooleanBuilder bidderIdEq(Long userId) {
        return nullSafeBuilder(() -> bid.bidderId.eq(userId));
    }

    private BooleanBuilder bidderIdEqSub(QBid qBid, Long userId) {
        return nullSafeBuilder(() -> qBid.bidderId.eq(userId));
    }

    private BooleanBuilder winnerIdEq(Long userId) {
        return nullSafeBuilder(() -> auctionV2.winnerId.isNotNull().and(auctionV2.winnerId.eq(userId)));
    }

    private BooleanBuilder likeUserIdEq(Long userId) {
        return nullSafeBuilder(() -> likeV2.userId.eq(userId));
    }

    private BooleanBuilder categoryEqIgnoreNull(Category category) {
        return nullSafeBuilderIgnore(() -> auctionV2.category.eq(category));
    }

    private BooleanExpression timeRemainingIgnoreNull(Integer endWithinSeconds) {
        return endWithinSeconds != null ? timeRemaining().between(0, endWithinSeconds) : null;
    }

    private static NumberExpression<Integer> timeRemaining() {
        return numberTemplate(Integer.class,
                "GREATEST(0, TIMESTAMPDIFF(SECOND, CURRENT_TIMESTAMP, {0}))", auctionV2.endDateTime); // 음수면 0으로 처리
    }

    @Getter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public enum AuctionOrder implements QuerydslOrder {
        POPULARITY("popularity-v2", auctionV2.bidCount.desc()),
        EXPENSIVE("expensive-v2", auctionV2.minPrice.desc()),
        CHEAP("cheap-v2", auctionV2.minPrice.asc()),
        IMMEDIATELY("immediately-v2", timeRemaining().asc()),
        NEWEST("newest-v2", auctionV2.createdAt.desc());

        private final String name;
        private final OrderSpecifier<?> orderSpecifier;
    }
}
