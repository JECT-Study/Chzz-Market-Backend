package org.chzz.market.domain.auction.repository;

import static org.chzz.market.common.util.QuerydslUtil.nullSafeBuilder;
import static org.chzz.market.common.util.QuerydslUtil.nullSafeBuilderIgnore;
import static org.chzz.market.domain.auction.entity.AuctionStatus.ENDED;
import static org.chzz.market.domain.auction.entity.AuctionStatus.PRE;
import static org.chzz.market.domain.auction.entity.AuctionStatus.PROCEEDING;
import static org.chzz.market.domain.auction.entity.QAuction.auction;
import static org.chzz.market.domain.bid.entity.Bid.BidStatus.ACTIVE;
import static org.chzz.market.domain.bid.entity.Bid.BidStatus.CANCELLED;
import static org.chzz.market.domain.bid.entity.QBid.bid;
import static org.chzz.market.domain.image.entity.QImage.image;
import static org.chzz.market.domain.like.entity.QLike.like;
import static org.chzz.market.domain.order.entity.QOrder.order;
import static org.chzz.market.domain.user.entity.QUser.user;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Ops.DateTimeOps;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.DateTimeOperation;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.chzz.market.common.util.QuerydslOrder;
import org.chzz.market.common.util.QuerydslOrderProvider;
import org.chzz.market.domain.auction.dto.AuctionBidDetail;
import org.chzz.market.domain.auction.dto.AuctionLikeDetail;
import org.chzz.market.domain.auction.dto.response.EndedAuctionResponse;
import org.chzz.market.domain.auction.dto.response.LostAuctionResponse;
import org.chzz.market.domain.auction.dto.response.OfficialAuctionDetailResponse;
import org.chzz.market.domain.auction.dto.response.OfficialAuctionResponse;
import org.chzz.market.domain.auction.dto.response.PreAuctionDetailResponse;
import org.chzz.market.domain.auction.dto.response.PreAuctionResponse;
import org.chzz.market.domain.auction.dto.response.ProceedingAuctionResponse;
import org.chzz.market.domain.auction.dto.response.QWonAuctionDetailsResponse;
import org.chzz.market.domain.auction.dto.response.WonAuctionDetailsResponse;
import org.chzz.market.domain.auction.dto.response.WonAuctionResponse;
import org.chzz.market.domain.auction.entity.AuctionStatus;
import org.chzz.market.domain.auction.entity.Category;
import org.chzz.market.domain.bid.entity.QBid;
import org.chzz.market.domain.image.dto.response.ImageResponse;
import org.chzz.market.domain.image.dto.response.QImageResponse;
import org.chzz.market.domain.user.dto.response.ParticipationCountsResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AuctionQueryRepository {
    private final JPAQueryFactory jpaQueryFactory;
    private final QuerydslOrderProvider querydslOrderProvider;

    /**
     * 낙찰자 정보 조회
     */
    public Optional<WonAuctionDetailsResponse> findWinningBidById(Long auctionId) {
        return Optional.ofNullable(jpaQueryFactory.select(
                        new QWonAuctionDetailsResponse(auction.id, auction.name, image.cdnPath, bid.amount))
                .from(auction)
                .leftJoin(bid).on(bid.bidderId.eq(auction.winnerId)
                        .and(bid.auctionId.eq(auction.id)))
                .leftJoin(auction.images, image).on(image.sequence.eq(1))
                .where(auction.id.eq(auctionId))
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
                                auction.id,
                                user.nickname,
                                user.profileImageUrl,
                                auction.name,
                                auction.description,
                                auction.minPrice,
                                userIdEq(userId),
                                auction.status,
                                auction.category,
                                auction.updatedAt,
                                auction.likeCount,
                                like.id.isNotNull()
                        )
                )
                .from(auction)
                .join(auction.seller, user)
                .leftJoin(like).on(like.auctionId.eq(auction.id).and(likeUserIdEq(userId)))
                .where(auction.id.eq(auctionId))
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
                                auction.id,
                                user.nickname,
                                user.profileImageUrl,
                                auction.name,
                                auction.description,
                                auction.minPrice,
                                userIdEq(userId),
                                auction.status,
                                auction.category,
                                timeRemaining().longValue(),
                                auction.bidCount,
                                activeBid.id.isNotNull(),
                                activeBid.id,
                                activeBid.amount.coalesce(0L),
                                activeBid.count.coalesce(3),
                                canceledBid.id.isNotNull(),
                                winnerIdEq(userId),
                                auction.winnerId.isNotNull(),
                                order.isNotNull()
                        )
                )
                .from(auction)
                .join(auction.seller, user)
                .leftJoin(activeBid).on(activeBid.auctionId.eq(auctionId) // 활성화된 입찰 조인
                        .and(activeBid.status.eq(ACTIVE))
                        .and(bidderIdEqSub(activeBid, userId)))
                .leftJoin(canceledBid).on(canceledBid.auctionId.eq(auctionId) // 취소된 입찰 조인
                        .and(canceledBid.status.eq(CANCELLED))
                        .and(bidderIdEqSub(canceledBid, userId)))
                .leftJoin(order).on(order.auction.eq(auction))
                .where(auction.id.eq(auctionId))
                .fetchOne());

        officialAuctionDetailResponse.ifPresent(
                response -> response.addImageList(getImagesByAuctionId(response.getAuctionId())));

        return officialAuctionDetailResponse;
    }

    /**
     * 사전 경매 목록 조회
     */
    public Page<PreAuctionResponse> findPreAuctions(Long userId, Category category, Pageable pageable) {
        JPAQuery<?> baseQuery = jpaQueryFactory.from(auction)
                .where(categoryEqIgnoreNull(category).and(auction.status.eq(PRE)));

        List<PreAuctionResponse> content = baseQuery
                .select(
                        Projections.constructor(
                                PreAuctionResponse.class,
                                auction.id,
                                auction.name,
                                image.cdnPath,
                                auction.minPrice.longValue(),
                                userIdEq(userId),
                                auction.likeCount,
                                like.id.isNotNull()
                        )
                )
                .join(auction.seller, user)
                .leftJoin(auction.images, image).on(image.sequence.eq(1))
                .leftJoin(like).on(like.auctionId.eq(auction.id).and(likeUserIdEq(userId)))
                .orderBy(querydslOrderProvider.getOrderSpecifiers(pageable))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = baseQuery.select(auction.count());

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    /**
     * 정식 경매 목록 조회
     */
    public Page<OfficialAuctionResponse> findOfficialAuctions(Long userId, Category category, AuctionStatus status,
                                                              Integer endWithinSeconds,
                                                              Pageable pageable) {
        JPAQuery<?> baseQuery = jpaQueryFactory.from(auction)
                .where(categoryEqIgnoreNull(category).and(auction.status.eq(status))
                        .and(timeRemainingIgnoreNull(endWithinSeconds)));

        List<OfficialAuctionResponse> content = baseQuery
                .select(
                        Projections.constructor(
                                OfficialAuctionResponse.class,
                                auction.id,
                                auction.name,
                                image.cdnPath,
                                auction.minPrice.longValue(),
                                userIdEq(userId),
                                timeRemaining().longValue(),
                                auction.bidCount,
                                bid.id.isNotNull()
                        )
                )
                .join(auction.seller, user)
                .leftJoin(bid).on(bid.auctionId.eq(auction.id).and(bidderIdEq(userId)).and(bid.status.eq(ACTIVE)))
                .leftJoin(auction.images, image).on(image.sequence.eq(1))
                .orderBy(querydslOrderProvider.getOrderSpecifiers(pageable))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = baseQuery.select(auction.count());

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    /**
     * 사용자가 등록한 사전경매 목록 조회
     */
    public Page<PreAuctionResponse> findPreAuctionsByUserId(Long userId, Pageable pageable) {
        JPAQuery<?> baseQuery = jpaQueryFactory.from(auction)
                .join(auction.seller, user).on(user.id.eq(userId))
                .where(auction.status.eq(PRE));

        List<PreAuctionResponse> content = baseQuery
                .select(
                        Projections.constructor(
                                PreAuctionResponse.class,
                                auction.id,
                                auction.name,
                                image.cdnPath,
                                auction.minPrice.longValue(),
                                Expressions.TRUE,
                                auction.likeCount,
                                Expressions.FALSE
                        )
                )
                .leftJoin(auction.images, image).on(image.sequence.eq(1))
                .orderBy(querydslOrderProvider.getOrderSpecifiers(pageable))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = baseQuery.select(auction.count());

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    /**
     * 사용자가 좋아요한 사전 경매목록 조회
     */
    public Page<PreAuctionResponse> findLikedAuctionsByUserId(Long userId, Pageable pageable) {
        JPAQuery<?> baseQuery = jpaQueryFactory.from(auction)
                .join(like).on(like.auctionId.eq(auction.id).and(like.userId.eq(userId)))
                .where(auction.status.eq(PRE));

        List<PreAuctionResponse> content = baseQuery
                .select(
                        Projections.constructor(
                                PreAuctionResponse.class,
                                auction.id,
                                auction.name,
                                image.cdnPath,
                                auction.minPrice.longValue(),
                                userIdEq(userId),
                                auction.likeCount,
                                like.id.isNotNull()
                        )
                )
                .join(auction.seller, user)
                .leftJoin(auction.images, image).on(image.sequence.eq(1))
                .orderBy(querydslOrderProvider.getOrderSpecifiers(pageable))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = baseQuery.select(auction.count());

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    /**
     * 사용자가 등록한 진행 중인 경매 목록 조회
     */
    public Page<ProceedingAuctionResponse> findProceedingAuctionsByUserId(Long userId, Pageable pageable) {
        JPAQuery<?> baseQuery = jpaQueryFactory.from(auction)
                .join(auction.seller, user).on(user.id.eq(userId))
                .where(auction.status.eq(PROCEEDING));

        List<ProceedingAuctionResponse> content = baseQuery
                .select(
                        Projections.constructor(
                                ProceedingAuctionResponse.class,
                                auction.id,
                                auction.name,
                                image.cdnPath,
                                auction.minPrice.longValue(),
                                userIdEq(userId),
                                timeRemaining().longValue(),
                                auction.status,
                                auction.bidCount,
                                auction.createdAt
                        )
                )
                .leftJoin(auction.images, image).on(image.sequence.eq(1))
                .orderBy(querydslOrderProvider.getOrderSpecifiers(pageable))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = baseQuery.select(auction.count());

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    /**
     * 사용자가 등록한 종료된 경매 목록 조회
     */
    public Page<EndedAuctionResponse> findEndedAuctionsByUserId(Long userId, Pageable pageable) {
        JPAQuery<?> baseQuery = jpaQueryFactory.from(auction)
                .join(auction.seller, user).on(user.id.eq(userId))
                .where(auction.status.eq(ENDED));

        List<EndedAuctionResponse> content = baseQuery
                .select(
                        Projections.constructor(
                                EndedAuctionResponse.class,
                                auction.id,
                                auction.name,
                                image.cdnPath,
                                auction.minPrice.longValue(),
                                userIdEq(userId),
                                auction.bidCount,
                                getWinningBidAmount(),
                                auction.winnerId.isNotNull(),
                                order.isNotNull(),
                                auction.createdAt
                        )
                )
                .leftJoin(auction.images, image).on(image.sequence.eq(1))
                .leftJoin(order).on(order.auction.eq(auction))
                .orderBy(querydslOrderProvider.getOrderSpecifiers(pageable))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = baseQuery.select(auction.count());

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    /**
     * 사용자가 낙찰한 경매 목록 조회
     */
    public Page<WonAuctionResponse> findWonAuctionsByUserId(Long userId, Pageable pageable) {
        JPAQuery<?> baseQuery = jpaQueryFactory.from(auction)
                .join(bid).on(bid.auctionId.eq(auction.id).and(bid.bidderId.eq(userId).and(bid.status.eq(ACTIVE))))
                .where(auction.winnerId.eq(userId).and(auction.status.eq(ENDED)));

        List<WonAuctionResponse> content = baseQuery
                .select(
                        Projections.constructor(
                                WonAuctionResponse.class,
                                auction.id,
                                auction.name,
                                image.cdnPath,
                                auction.minPrice.longValue(),
                                userIdEq(userId),
                                auction.bidCount,
                                auction.endDateTime,
                                bid.amount,
                                order.isNotNull(),
                                order.id
                        )
                )
                .join(auction.seller, user)
                .leftJoin(order).on(order.auction.id.eq(auction.id))
                .leftJoin(auction.images, image).on(image.sequence.eq(1))
                .orderBy(querydslOrderProvider.getOrderSpecifiers(pageable))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = baseQuery.select(auction.count());

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }


    /**
     * 사용자가 낙찰 실패한 경매 목록 조회
     */
    public Page<LostAuctionResponse> findLostAuctionsByUserId(Long userId, Pageable pageable) {
        JPAQuery<?> baseQuery = jpaQueryFactory.from(auction)
                .join(bid).on(bid.auctionId.eq(auction.id).and(bid.bidderId.eq(userId).and(bid.status.eq(ACTIVE))))
                .where(auction.winnerId.ne(userId).and(auction.status.eq(ENDED)));

        List<LostAuctionResponse> content = baseQuery
                .select(
                        Projections.constructor(
                                LostAuctionResponse.class,
                                auction.id,
                                auction.name,
                                image.cdnPath,
                                auction.minPrice.longValue(),
                                userIdEq(userId),
                                auction.bidCount,
                                auction.endDateTime,
                                bid.amount
                        )
                )
                .join(auction.seller, user)
                .leftJoin(auction.images, image).on(image.sequence.eq(1))
                .orderBy(querydslOrderProvider.getOrderSpecifiers(pageable))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = baseQuery.select(auction.count());

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    /**
     * 사용자가 참여한 경매 통계 조회
     */
    public ParticipationCountsResponse getParticipationCounts(Long userId) {
        DateTimeOperation<LocalDateTime> now = Expressions.dateTimeOperation(LocalDateTime.class,
                DateTimeOps.CURRENT_TIMESTAMP);

        BooleanExpression isEnded = auction.status.eq(ENDED)
                .and(auction.endDateTime.before(now));

        // 사용자가 참여한 경매 ID 목록을 가져옵니다.
        List<Long> participatedAuctionIds = jpaQueryFactory
                .select(auction.id)
                .from(auction)
                .join(bid).on(bid.auctionId.eq(auction.id))
                .where(bid.bidderId.eq(userId)
                        .and(bid.status.eq(ACTIVE)))
                .fetch();

        BooleanExpression isParticipatedAuction = auction.id.in(participatedAuctionIds);

        Long proceedingCount = Optional.ofNullable(jpaQueryFactory
                        .select(auction.count())
                        .from(auction)
                        .where(isParticipatedAuction
                                .and(auction.status.eq(PROCEEDING))
                                .and(auction.endDateTime.after(now)))
                        .fetchFirst())
                .orElse(0L);

        Long successCount = Optional.ofNullable(jpaQueryFactory
                        .select(auction.count())
                        .from(auction)
                        .where(isParticipatedAuction
                                .and(auction.winnerId.eq(userId))
                                .and(isEnded))
                        .fetchFirst())
                .orElse(0L);

        Long failureCount = Optional.ofNullable(jpaQueryFactory
                        .select(auction.count())
                        .from(auction)
                        .where(isParticipatedAuction
                                .and(auction.winnerId.ne(userId))
                                .and(isEnded))
                        .fetchFirst())
                .orElse(0L);

        return new ParticipationCountsResponse(
                proceedingCount,
                successCount,
                failureCount
        );
    }

    /**
     * 현재 사용자가 입찰 진행 중인 경매 갯수 조회
     */
    public long countProceedingAuctionsByUserId(Long userId) {
        return jpaQueryFactory
                .select(auction.count())
                .from(auction)
                .join(bid).on(bid.auctionId.eq(auction.id)).on(bid.bidderId.eq(userId).and(bid.status.eq(ACTIVE)))
                .where(auction.status.eq(PROCEEDING))
                .fetchOne();
    }


    public List<AuctionLikeDetail> findAuctionLikeDetailsByAuctionIds(List<Long> auctionIds, Long userId) {
        return jpaQueryFactory
                .select(
                        Projections.constructor(
                                AuctionLikeDetail.class,
                                auction.id,
                                auction.likeCount,
                                like.id.isNotNull()
                        )
                )
                .from(auction)
                .leftJoin(like).on(like.auctionId.eq(auction.id).and(likeUserIdEq(userId)))
                .where(auction.id.in(auctionIds))
                .fetch();

    }

    public List<AuctionBidDetail> findAuctionBidDetailsByAuctionIds(List<Long> auctionIds, Long userId) {
        return jpaQueryFactory
                .select(
                        Projections.constructor(
                                AuctionBidDetail.class,
                                auction.id,
                                auction.bidCount,
                                bid.id.isNotNull()
                        )
                )
                .from(auction)
                .leftJoin(bid).on(bid.auctionId.eq(auction.id).and(bidderIdEq(userId)).and(bid.status.eq(ACTIVE)))
                .where(auction.id.in(auctionIds))
                .fetch();
    }

    private List<ImageResponse> getImagesByAuctionId(Long auctionId) {
        return jpaQueryFactory
                .select(new QImageResponse(image.id, image.cdnPath))
                .from(image)
                .where(image.auction.id.eq(auctionId))
                .orderBy(image.sequence.asc())
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
        return nullSafeBuilder(() -> auction.winnerId.isNotNull().and(auction.winnerId.eq(userId)));
    }

    private BooleanBuilder likeUserIdEq(Long userId) {
        return nullSafeBuilder(() -> like.userId.eq(userId));
    }

    private BooleanBuilder categoryEqIgnoreNull(Category category) {
        return nullSafeBuilderIgnore(() -> auction.category.eq(category));
    }

    private BooleanExpression timeRemainingIgnoreNull(Integer endWithinSeconds) {
        return endWithinSeconds != null ? timeRemaining().between(0, endWithinSeconds) : null;
    }

    private static NumberExpression<Integer> timeRemaining() {
        return Expressions.numberTemplate(Integer.class,
                "GREATEST(0, TIMESTAMPDIFF(SECOND, CURRENT_TIMESTAMP, {0}))", auction.endDateTime); // 음수면 0으로 처리
    }

    private JPQLQuery<Long> getWinningBidAmount() {
        return JPAExpressions.select(bid.amount.max().coalesce(0L))
                .from(bid)
                .where(
                        bid.auctionId.eq(auction.id),
                        bid.status.eq(ACTIVE)
                );
    }

    @Getter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public enum AuctionOrder implements QuerydslOrder {
        POPULARITY("popularity", auction.bidCount.desc()),
        LIKES("likes", auction.likeCount.desc()),
        EXPENSIVE("expensive", auction.minPrice.desc()),
        CHEAP("cheap", auction.minPrice.asc()),
        IMMEDIATELY("immediately", timeRemaining().asc()),
        NEWEST("newest", auction.createdAt.desc());

        private final String name;
        private final OrderSpecifier<?> orderSpecifier;
    }
}
