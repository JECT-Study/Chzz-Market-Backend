package org.chzz.market.domain.auction.repository;

import static org.chzz.market.domain.auction.entity.AuctionDocument.Constant.AUCTION_STATUS;
import static org.chzz.market.domain.auction.entity.AuctionDocument.Constant.CATEGORY;
import static org.chzz.market.domain.auction.entity.AuctionDocument.Constant.DESCRIPTION;
import static org.chzz.market.domain.auction.entity.AuctionDocument.Constant.END_DATE_TIME;
import static org.chzz.market.domain.auction.entity.AuctionDocument.Constant.IMAGE_URL;
import static org.chzz.market.domain.auction.entity.AuctionDocument.Constant.MIN_PRICE;
import static org.chzz.market.domain.auction.entity.AuctionDocument.Constant.NAME;
import static org.chzz.market.domain.auction.error.AuctionErrorCode.AUCTION_ELASTIC_ERROR;

import java.time.format.DateTimeFormatter;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.chzz.market.domain.auction.entity.AuctionDocument;
import org.chzz.market.domain.auction.entity.AuctionStatus;
import org.chzz.market.domain.auction.error.AuctionException;
import org.chzz.market.domain.auction.service.AuctionPageableAdjuster;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.UpdateQuery;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
@Slf4j
public class AuctionElasticQueryRepository {
    private static final String AUCTION_INDEX = "auction";

    private final AuctionPageableAdjuster pageableAdjuster;
    private final ElasticsearchOperations operations;

    /**
     * 경매 목록 검색 (키워드, 상태)
     */
    public SearchHits<AuctionDocument> searchAuctions(String keyword, AuctionStatus status, Pageable pageable) {
        Pageable adjustedPageable = pageableAdjuster.adjustPageable(pageable);
        NativeQuery query = NativeQuery.builder()
                .withQuery(q -> q.bool(b -> b
                        .must(m -> m.multiMatch(mm -> mm
                                .query(keyword)
                                .fields(NAME, DESCRIPTION, CATEGORY) // 멀티 매치 적용
                        ))
                        .filter(f -> f.term(t -> t
                                .field(AUCTION_STATUS)
                                .value(status.name()))) // status 필터링 적용
                ))
                .withPageable(adjustedPageable) // 페이징 적용
                .build();
        // 쿼리 실행
        return operations.search(query, AuctionDocument.class);
    }

    /**
     * 경매 문서 업데이트
     */
    public void update(AuctionDocument auctionDocument) {
        Map<String, Object> updateFields = Map.of(
                NAME, auctionDocument.getName(),
                DESCRIPTION, auctionDocument.getDescription(),
                MIN_PRICE, auctionDocument.getMinPrice(),
                CATEGORY, auctionDocument.getCategory(),
                IMAGE_URL, auctionDocument.getImageUrl()
        );
        executeUpdate(auctionDocument.getAuctionId(), updateFields);
    }

    /**
     * 경매 시작 업데이트
     */
    public void updateAuctionToStartedStatus(AuctionDocument auctionDocument) {
        String formattedDateTime = auctionDocument.getEndDateTime()
                .format(DateTimeFormatter.ofPattern(DateFormat.date_hour_minute_second.getPattern()));

        Map<String, Object> updateFields = Map.of(
                AUCTION_STATUS, auctionDocument.getAuctionStatus().name(),
                END_DATE_TIME, formattedDateTime
        );
        executeUpdate(auctionDocument.getAuctionId(), updateFields);
    }

    /**
     * 경매 종료 업데이트
     */
    public void updateAuctionToEndedStatus(AuctionDocument auctionDocument) {
        Map<String, Object> updateFields = Map.of(
                AUCTION_STATUS, auctionDocument.getAuctionStatus().name()
        );
        executeUpdate(auctionDocument.getAuctionId(), updateFields);
    }

    /**
     * 공통 업데이트 실행 메서드
     */
    private void executeUpdate(Long auctionId, Map<String, Object> updateFields) {
        try {
            UpdateQuery updateQuery = UpdateQuery.builder(String.valueOf(auctionId))
                    .withDocument(Document.from(updateFields))
                    .build();
            operations.update(updateQuery, IndexCoordinates.of(AUCTION_INDEX));
            log.info("Elasticsearch 문서 업데이트 성공: auctionId={}, updateFields={}", auctionId, updateFields);
        } catch (Exception e) {
            log.error("Elasticsearch 문서 업데이트 실패: auctionId={}, updateFields={}", auctionId, updateFields, e);
            throw new AuctionException(AUCTION_ELASTIC_ERROR);
        }
    }
}
