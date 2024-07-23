package org.chzz.market.domain.auction.repository;

import static org.chzz.market.domain.auction.entity.Auction.Status.*;
import static org.chzz.market.domain.auction.entity.QAuction.auction;
import static org.chzz.market.domain.bid.entity.QBid.bid;
import static org.chzz.market.domain.image.entity.QImage.image;
import static org.chzz.market.domain.product.entity.QProduct.product;
import static org.chzz.market.domain.product.entity.SortType.CHEAP;
import static org.chzz.market.domain.product.entity.SortType.EXPENSIVE;
import static org.chzz.market.domain.product.entity.SortType.NEWEST;
import static org.chzz.market.domain.product.entity.SortType.POPULARITY;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.chzz.market.domain.auction.dto.AuctionResponse;
import org.chzz.market.domain.auction.dto.QAuctionResponse;
import org.chzz.market.domain.auction.entity.Auction.Status;
import org.chzz.market.domain.image.entity.QImage;
import org.chzz.market.domain.product.entity.Product.Category;
import org.chzz.market.domain.product.entity.SortType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

@RequiredArgsConstructor
public class AuctionRepositoryImpl implements AuctionRepositoryCustom {
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Page<AuctionResponse> findAuctionsByCategory(Category category, SortType sortType, Long userId,
                                                        Pageable pageable) {
        QImage imageSub = new QImage("imageSub");

        List<AuctionResponse> content = jpaQueryFactory
                .select(new QAuctionResponse(
                        auction.id,
                        product.name,
                        image.cdnPath,
                        auction.createdAt,
                        auction.minPrice,
                        bid.countDistinct(),
                        isParticipating(userId)
                ))
                .from(auction)
                .join(auction.product, product)
                .leftJoin(image).on(image.product.id.eq(product.id).and(image.id.eq(getFirstImageId(imageSub))))
                .leftJoin(bid).on(bid.auction.id.eq((auction.id)))
                .where(auction.product.category.eq(category))
                .where(auction.status.eq(PROCEEDING))
                .groupBy(auction.id, product.name, image.cdnPath, auction.createdAt, auction.minPrice)
                .orderBy(getOrderSpecifier(sortType))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = jpaQueryFactory
                .select(auction.id.count())
                .from(auction)
                .join(auction.product, product)
                .leftJoin(bid).on(bid.auction.id.eq(auction.id))
                .where(auction.product.category.eq(category))
                .where(auction.status.eq(PROCEEDING));

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchCount);
    }

    /**
     * 상품의 첫 번째 이미지 조회
     *
     * @param imageSub
     * @return
     */
    private JPQLQuery<Long> getFirstImageId(QImage imageSub) {
        return JPAExpressions.select(imageSub.id.min())
                .from(imageSub)
                .where(imageSub.product.id.eq(product.id));
    }

    /**
     * 사용자가 참여 중인 경매인지 확인
     *
     * @param userId
     * @return
     */
    private BooleanExpression isParticipating(Long userId) {
        return JPAExpressions.selectFrom(bid)
                .where(bid.auction.id.eq(auction.id).and(bid.bidder.id.eq(userId)))
                .exists();
    }

    /**
     * 정렬 조건에 따른 OrderSpecifier 반환
     *
     * @param sortType
     * @return OrderSpecifier
     */
    private OrderSpecifier<?> getOrderSpecifier(SortType sortType) {
        Map<SortType, OrderSpecifier<?>> orderSpecifierMap = Map.of(
                POPULARITY, bid.countDistinct().desc(),
                EXPENSIVE, auction.minPrice.desc(),
                CHEAP, auction.minPrice.asc(),
                NEWEST, auction.createdAt.desc()
        );
        return orderSpecifierMap.getOrDefault(sortType, auction.createdAt.desc());
    }
}
