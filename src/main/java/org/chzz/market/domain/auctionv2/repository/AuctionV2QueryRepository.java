package org.chzz.market.domain.auctionv2.repository;

import static org.chzz.market.domain.auctionv2.entity.QAuctionV2.auctionV2;
import static org.chzz.market.domain.bid.entity.QBid.bid;
import static org.chzz.market.domain.image.entity.QImageV2.imageV2;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.chzz.market.domain.auctionv2.dto.response.QWonAuctionDetailsResponse;
import org.chzz.market.domain.auctionv2.dto.response.WonAuctionDetailsResponse;
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

    private BooleanExpression isRepresentativeImage() {
        return imageV2.auction.eq(auctionV2).and(imageV2.sequence.eq(1));
    }

}
