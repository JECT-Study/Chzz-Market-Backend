package org.chzz.market.domain.bid.repository;

import static org.chzz.market.domain.auctionv2.entity.QAuctionV2.auctionV2;
import static org.chzz.market.domain.bid.entity.Bid.BidStatus.ACTIVE;
import static org.chzz.market.domain.bid.entity.QBid.bid;
import static org.chzz.market.domain.user.entity.QUser.user;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.chzz.market.common.util.QuerydslOrderProvider;
import org.chzz.market.domain.bid.dto.response.BidInfoResponse;
import org.chzz.market.domain.bid.dto.response.QBidInfoResponse;
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
}
