package org.chzz.market.domain.auctionv2.repository;

import static com.querydsl.core.types.dsl.Expressions.numberTemplate;
import static org.chzz.market.common.util.QuerydslUtil.nullSafeBuilder;
import static org.chzz.market.domain.auctionv2.entity.QAuctionV2.auctionV2;
import static org.chzz.market.domain.bid.entity.Bid.BidStatus.ACTIVE;
import static org.chzz.market.domain.bid.entity.Bid.BidStatus.CANCELLED;
import static org.chzz.market.domain.bid.entity.QBid.bid;
import static org.chzz.market.domain.image.entity.QImageV2.imageV2;
import static org.chzz.market.domain.likev2.entity.QLikeV2.likeV2;
import static org.chzz.market.domain.orderv2.entity.QOrderV2.orderV2;
import static org.chzz.market.domain.user.entity.QUser.user;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.chzz.market.domain.auctionv2.dto.response.OfficialAuctionDetailResponse;
import org.chzz.market.domain.auctionv2.dto.response.PreAuctionDetailResponse;
import org.chzz.market.domain.auctionv2.dto.response.QWonAuctionDetailsResponse;
import org.chzz.market.domain.auctionv2.dto.response.WonAuctionDetailsResponse;
import org.chzz.market.domain.bid.entity.QBid;
import org.chzz.market.domain.image.dto.ImageResponse;
import org.chzz.market.domain.image.dto.QImageResponse;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AuctionV2QueryRepository {
    private final JPAQueryFactory jpaQueryFactory;

    /**
     * 낙찰자 정보 조회
     */
    public Optional<WonAuctionDetailsResponse> findWinningBidById(Long auctionId) {
        return Optional.ofNullable(jpaQueryFactory.select(
                        new QWonAuctionDetailsResponse(auctionV2.id, auctionV2.name, imageV2.cdnPath, bid.amount))
                .from(auctionV2)
                .leftJoin(bid).on(bid.bidderId.eq(auctionV2.winnerId)
                        .and(bid.auctionId.eq(auctionV2.id)))
                .leftJoin(imageV2).on(isRepresentativeImage())
                .where(auctionV2.id.eq(auctionId))
                .fetchOne());
    }

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
                                isAuctionLikedByUserId(userId)
                        )
                )
                .from(auctionV2)
                .join(auctionV2.seller, user)
                .where(auctionV2.id.eq(auctionId))
                .fetchOne());

        result.ifPresent(response -> response.addImageList(getImagesByAuctionId(response.getAuctionId())));
        return result;
    }

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

    private List<ImageResponse> getImagesByAuctionId(Long auctionId) {
        return jpaQueryFactory
                .select(new QImageResponse(imageV2.id, imageV2.cdnPath))
                .from(imageV2)
                .where(imageV2.auction.id.eq(auctionId))
                .orderBy(imageV2.sequence.asc())
                .fetch();
    }

    private BooleanExpression isAuctionLikedByUserId(Long userId) {
        return JPAExpressions.selectOne()
                .from(likeV2)
                .where(likeV2.auctionId.eq(auctionV2.id)
                        .and(likeUserIdEq(userId)))
                .exists();
    }

    private BooleanExpression isRepresentativeImage() {
        return imageV2.auction.eq(auctionV2).and(imageV2.sequence.eq(1));
    }

    private BooleanBuilder userIdEq(Long userId) {
        return nullSafeBuilder(() -> user.id.eq(userId));
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

    private static NumberExpression<Integer> timeRemaining() {
        return numberTemplate(Integer.class,
                "GREATEST(0, TIMESTAMPDIFF(SECOND, CURRENT_TIMESTAMP, {0}))", auctionV2.endDateTime); // 음수면 0으로 처리
    }
}
