package org.chzz.market.domain.bid.repository;

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
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.chzz.market.common.util.QuerydslOrder;
import org.chzz.market.common.util.QuerydslOrderProvider;
import org.chzz.market.domain.auction.entity.Auction;
import org.chzz.market.domain.bid.dto.query.BiddingRecord;
import org.chzz.market.domain.bid.dto.query.QBiddingRecord;
import org.chzz.market.domain.bid.dto.response.BidInfoResponse;
import org.chzz.market.domain.bid.dto.response.QBidInfoResponse;
import org.chzz.market.domain.bid.entity.Bid;
import org.chzz.market.domain.bid.entity.Bid.BidStatus;
import org.chzz.market.domain.image.entity.QImage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

@RequiredArgsConstructor
public class BidRepositoryCustomImpl implements BidRepositoryCustom {
    private final JPAQueryFactory jpaQueryFactory;
    private final QuerydslOrderProvider querydslOrderProvider;

    public Page<BiddingRecord> findUsersBidHistory(Long userId, Pageable pageable) {
        QImage firstImage = new QImage("firstImage");

        JPQLQuery<BiddingRecord> baseQuery = getBaseQuery(firstImage, userId);
        List<BiddingRecord> content = baseQuery
                .orderBy(querydslOrderProvider.getOrderSpecifiers(pageable))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        return PageableExecutionUtils.getPage(content, pageable, getCount(userId)::fetchOne);
    }

    @Override
    public List<Bid> findAllBidsByAuction(Auction auction) {
        return jpaQueryFactory
                .selectFrom(bid)
                .leftJoin(bid.bidder).fetchJoin()
                .where(
                        bid.auction.eq(auction).and(bid.status.eq(BidStatus.ACTIVE))
                )
                .orderBy(bid.amount.desc(), bid.updatedAt.asc())
                .fetch();
    }

    @Override
    public Page<BidInfoResponse> findBidsByAuctionId(Long auctionId, Pageable pageable) {
        BooleanExpression isWinner = auction.winnerId.isNotNull().and(auction.winnerId.eq(user.id)).or(auction.winnerId.isNull().and(Expressions.FALSE));

        JPAQuery<?> baseQuery = jpaQueryFactory.from(bid)
                .join(bid.auction, auction).on(auction.id.eq(auctionId).and(bid.status.eq(BidStatus.ACTIVE)));

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

    private JPQLQuery<BiddingRecord> getBaseQuery(QImage firstImage, Long userId) {
        return jpaQueryFactory
                .select(new QBiddingRecord(
                        product.name,
                        product.minPrice.longValue(),
                        bid.amount,
                        auction.bids.size().longValue(),
                        firstImage.cdnPath,
                        timeRemaining().longValue()
                ))
                .from(bid)
                .join(bid.auction, auction)
                .on(bid.bidder.id.eq(userId).and(bid.status.eq(BidStatus.ACTIVE)))
                .leftJoin(auction.product, product)
                .leftJoin(firstImage)
                .on(firstImage.product.eq(product).and(firstImage.id.eq(
                        JPAExpressions
                                .select(image.id.min())
                                .from(image)
                                .where(image.product.eq(product))
                )))
                .groupBy(product.name,
                        product.minPrice,
                        bid.amount,
                        auction.bids.size(),
                        firstImage.cdnPath,
                        auction.createdAt,
                        auction.id);
    }

    private JPAQuery<Long> getCount(Long userId) {
        return jpaQueryFactory
                .select(bid.count())
                .from(bid)
                .join(bid.auction, auction)
                .on(bid.bidder.id.eq(userId).and(bid.status.eq(BidStatus.ACTIVE)));
    }

    private static NumberExpression<Integer> timeRemaining() {
        return Expressions.numberTemplate(Integer.class,
                "GREATEST(0, TIMESTAMPDIFF(SECOND, CURRENT_TIMESTAMP, {0}))", auction.endDateTime); // 음수면 0으로 처리
    }

    @Getter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public enum BidOrder implements QuerydslOrder {
        AMOUNT("bidAmount", bid.amount.asc()),
        TIME_REMAINING("time-remaining", auction.endDateTime.desc());

        private final String name;
        private final OrderSpecifier<?> orderSpecifier;
    }
}
