package org.chzz.market.domain.bid.repository;

import static org.chzz.market.common.util.QuerydslUtil.nullSafeBuilderIgnore;
import static org.chzz.market.domain.auction.entity.QAuction.auction;
import static org.chzz.market.domain.bid.entity.Bid.BidStatus.ACTIVE;
import static org.chzz.market.domain.bid.entity.QBid.bid;
import static org.chzz.market.domain.image.entity.QImage.image;
import static org.chzz.market.domain.product.entity.QProduct.product;
import static org.chzz.market.domain.user.entity.QUser.user;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
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
import org.chzz.market.domain.auction.type.AuctionStatus;
import org.chzz.market.domain.bid.dto.query.BiddingRecord;
import org.chzz.market.domain.bid.dto.query.QBiddingRecord;
import org.chzz.market.domain.bid.dto.response.BidInfoResponse;
import org.chzz.market.domain.bid.dto.response.QBidInfoResponse;
import org.chzz.market.domain.bid.entity.Bid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

@RequiredArgsConstructor
public class BidRepositoryCustomImpl implements BidRepositoryCustom {
    private final JPAQueryFactory jpaQueryFactory;
    private final QuerydslOrderProvider querydslOrderProvider;

    public Page<BiddingRecord> findUsersBidHistory(Long userId, Pageable pageable, AuctionStatus auctionStatus) {
        // 공통된 부분을 baseQuery로 추출
        JPAQuery<?> baseQuery = jpaQueryFactory
                .from(bid)
                .join(bid.auction, auction).on(bid.bidder.id.eq(userId).and(bid.status.eq(ACTIVE)
                        .and(auctionStatusEqIgnoreNull(auctionStatus))));

        List<BiddingRecord> result = baseQuery
                .select(new QBiddingRecord(
                        auction.id,
                        product.name,
                        product.minPrice.longValue(),
                        bid.amount,
                        getBidCount(),
                        image.cdnPath,
                        timeRemaining().longValue()
                ))
                .leftJoin(auction.product, product)
                .leftJoin(image).on(image.product.eq(product).and(isRepresentativeImage()))
                .orderBy(querydslOrderProvider.getOrderSpecifiers(pageable))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 카운트 쿼리 작성
        JPAQuery<Long> countQuery = baseQuery
                .select(bid.count());

        return PageableExecutionUtils.getPage(result, pageable, countQuery::fetchOne);
    }

    @Override
    public List<Bid> findAllBidsByAuction(Auction auction) {
        return jpaQueryFactory
                .selectFrom(bid)
                .leftJoin(bid.bidder).fetchJoin()
                .where(
                        bid.auction.eq(auction).and(bid.status.eq(ACTIVE))
                )
                .orderBy(bid.amount.desc(), bid.updatedAt.asc())
                .fetch();
    }

    @Override
    public Page<BidInfoResponse> findBidsByAuctionId(Long auctionId, Pageable pageable) {
        BooleanExpression isWinner = auction.winnerId.isNotNull().and(auction.winnerId.eq(user.id))
                .or(auction.winnerId.isNull().and(Expressions.FALSE));

        JPAQuery<?> baseQuery = jpaQueryFactory.from(bid)
                .join(bid.auction, auction).on(auction.id.eq(auctionId).and(bid.status.eq(ACTIVE)));

        List<BidInfoResponse> content = baseQuery.select(new QBidInfoResponse(
                        bid.amount,
                        user.nickname,
                        isWinner
                ))
                .join(bid.bidder, user)
                .orderBy(querydslOrderProvider.getOrderSpecifiers(pageable))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = baseQuery.
                select(bid.count());
        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchCount);
    }

    /**
     * 상품의 대표 이미지를 조회하기 위한 조건을 반환합니다.
     *
     * @return 대표 이미지(첫 번째 이미지)의 sequence가 1인 조건식
     */
    private BooleanExpression isRepresentativeImage() {
        return image.sequence.eq(1);
    }

    private JPQLQuery<Long> getBidCount() {
        return JPAExpressions
                .select(bid.count())
                .from(bid)
                .where(bid.auction.id.eq(auction.id).and(bid.status.eq(ACTIVE)));
    }

    private static NumberExpression<Integer> timeRemaining() {
        return Expressions.numberTemplate(Integer.class,
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
