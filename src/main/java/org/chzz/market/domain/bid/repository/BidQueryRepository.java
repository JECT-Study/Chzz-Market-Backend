package org.chzz.market.domain.bid.repository;

import static com.querydsl.core.types.dsl.Expressions.numberTemplate;
import static org.chzz.market.common.util.QuerydslUtil.nullSafeBuilderIgnore;
import static org.chzz.market.domain.auctionv2.entity.QAuctionV2.auctionV2;
import static org.chzz.market.domain.bid.entity.Bid.BidStatus.ACTIVE;
import static org.chzz.market.domain.bid.entity.QBid.bid;
import static org.chzz.market.domain.image.entity.QImageV2.imageV2;
import static org.chzz.market.domain.user.entity.QUser.user;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.chzz.market.common.util.QuerydslOrderProvider;
import org.chzz.market.domain.auctionv2.entity.AuctionStatus;
import org.chzz.market.domain.auctionv2.entity.AuctionV2;
import org.chzz.market.domain.bid.dto.query.BiddingRecord;
import org.chzz.market.domain.bid.dto.query.QBiddingRecord;
import org.chzz.market.domain.bid.dto.response.BidInfoResponse;
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
        BooleanExpression isWinner = auctionV2.winnerId.isNotNull().and(auctionV2.winnerId.eq(user.id));

        JPAQuery<?> baseQuery = jpaQueryFactory.from(bid)
                .join(auctionV2).on(bid.auctionId.eq(auctionV2.id)
                        .and(auctionV2.id.eq(auctionId))
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
                .join(auctionV2).on(bid.auctionId.eq(auctionV2.id)
                        .and(bid.bidderId.eq(userId))
                        .and(bid.status.eq(ACTIVE))
                        .and(auctionStatusEqIgnoreNull(auctionStatus)));

        List<BiddingRecord> result = baseQuery
                .select(new QBiddingRecord(
                        auctionV2.id,
                        auctionV2.name,
                        auctionV2.minPrice.longValue(),
                        bid.amount,
                        auctionV2.bidCount,
                        imageV2.cdnPath,
                        timeRemaining().longValue()
                ))
                .leftJoin(imageV2).on(imageV2.auction.eq(auctionV2).and(isRepresentativeImage()))
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
    public List<Bid> findAllBidsByAuction(AuctionV2 auction) {
        return jpaQueryFactory
                .selectFrom(bid)
                .where(bid.auctionId.eq(auction.getId()).and(bid.status.eq(ACTIVE)))
                .orderBy(bid.amount.desc(), bid.updatedAt.asc())
                .fetch();
    }

    private BooleanExpression isRepresentativeImage() {
        return imageV2.auction.eq(auctionV2).and(imageV2.sequence.eq(1));
    }

    private static NumberExpression<Integer> timeRemaining() {
        return numberTemplate(Integer.class,
                "GREATEST(0, TIMESTAMPDIFF(SECOND, CURRENT_TIMESTAMP, {0}))", auctionV2.endDateTime); // 음수면 0으로 처리
    }

    private BooleanBuilder auctionStatusEqIgnoreNull(AuctionStatus status) {
        return nullSafeBuilderIgnore(() -> auctionV2.status.eq(status));
    }
}
