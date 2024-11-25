package org.chzz.market.domain.bid.repository;

import static com.querydsl.core.types.dsl.Expressions.numberTemplate;
import static org.chzz.market.common.util.QuerydslUtil.nullSafeBuilderIgnore;
import static org.chzz.market.domain.auction.entity.QAuction.auction;
import static org.chzz.market.domain.bid.entity.Bid.BidStatus.ACTIVE;
import static org.chzz.market.domain.bid.entity.QBid.bid;
import static org.chzz.market.domain.image.entity.QImage.image;
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
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.chzz.market.common.util.QuerydslOrder;
import org.chzz.market.common.util.QuerydslOrderProvider;
import org.chzz.market.domain.auction.entity.Auction;
import org.chzz.market.domain.auction.entity.AuctionStatus;
import org.chzz.market.domain.bid.dto.response.BidInfoResponse;
import org.chzz.market.domain.bid.dto.response.BiddingRecord;
import org.chzz.market.domain.bid.dto.response.QBidInfoResponse;
import org.chzz.market.domain.bid.entity.Bid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class BidQueryRepository {
    private final JPAQueryFactory jpaQueryFactory;
    private final QuerydslOrderProvider querydslOrderProvider;

    /**
     * 특정 경매의 입찰 조회
     */
    public Page<BidInfoResponse> findBidsByAuctionId(Long auctionId, Pageable pageable) {
        BooleanExpression isWinner = auction.winnerId.isNotNull().and(auction.winnerId.eq(user.id));

        JPAQuery<?> baseQuery = jpaQueryFactory.from(bid)
                .join(auction).on(bid.auctionId.eq(auction.id)
                        .and(auction.id.eq(auctionId))
                        .and(bid.status.eq(ACTIVE)));

        List<BidInfoResponse> content = baseQuery
                .select(new QBidInfoResponse(
                        bid.amount,
                        user.nickname,
                        isWinner
                ))
                .join(user).on(bid.bidderId.eq(user.id))
                .orderBy(querydslOrderProvider.getOrderSpecifiers(pageable))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = baseQuery.
                select(bid.count());
        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    /**
     * 나의 입찰 목록 조회
     */
    public Page<BiddingRecord> findUsersBidHistory(Long userId, Pageable pageable, AuctionStatus auctionStatus) {
        // 공통된 부분을 baseQuery로 추출
        JPAQuery<?> baseQuery = jpaQueryFactory
                .from(bid)
                .join(auction).on(bid.auctionId.eq(auction.id)
                        .and(bid.bidderId.eq(userId))
                        .and(bid.status.eq(ACTIVE))
                        .and(auctionStatusEqIgnoreNull(auctionStatus)));

        List<BiddingRecord> result = baseQuery
                .select(Projections.constructor(
                        BiddingRecord.class,
                        auction.id,
                        auction.name,
                        image.cdnPath,
                        auction.minPrice.longValue(),
                        Expressions.FALSE,
                        timeRemaining().longValue(),
                        auction.bidCount,
                        bid.amount
                ))
                .leftJoin(image).on(image.auction.eq(auction).and(isRepresentativeImage()))
                .orderBy(querydslOrderProvider.getOrderSpecifiers(pageable))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 카운트 쿼리 작성
        JPAQuery<Long> countQuery = baseQuery
                .select(bid.count());

        return PageableExecutionUtils.getPage(result, pageable, countQuery::fetchOne);
    }

    /**
     * 특정 경매의 모든 입찰 Entity 조회
     */
    public List<Bid> findAllBidsByAuction(Auction auction) {
        return jpaQueryFactory
                .selectFrom(bid)
                .where(bid.auctionId.eq(auction.getId()).and(bid.status.eq(ACTIVE)))
                .orderBy(bid.amount.desc(), bid.updatedAt.asc())
                .fetch();
    }

    private BooleanExpression isRepresentativeImage() {
        return image.auction.eq(auction).and(image.sequence.eq(1));
    }

    private static NumberExpression<Integer> timeRemaining() {
        return numberTemplate(Integer.class,
                "GREATEST(0, TIMESTAMPDIFF(SECOND, CURRENT_TIMESTAMP, {0}))", auction.endDateTime); // 음수면 0으로 처리
    }

    private BooleanBuilder auctionStatusEqIgnoreNull(AuctionStatus status) {
        return nullSafeBuilderIgnore(() -> auction.status.eq(status));
    }

    @Getter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public enum BidOrder implements QuerydslOrder {
        AMOUNT("bid-amount", bid.amount.asc()),
        TIME_REMAINING("time-remaining", auction.endDateTime.desc());

        private final String name;
        private final OrderSpecifier<?> orderSpecifier;
    }
}
