package org.chzz.market.domain.auction.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.chzz.market.domain.auction.dto.AuctionBidDetail;
import org.chzz.market.domain.auction.dto.AuctionLikeDetail;
import org.chzz.market.domain.auction.dto.response.OfficialAuctionResponse;
import org.chzz.market.domain.auction.dto.response.PreAuctionResponse;
import org.chzz.market.domain.auction.entity.AuctionDocument;
import org.chzz.market.domain.auction.entity.AuctionStatus;
import org.chzz.market.domain.auction.repository.AuctionElasticQueryRepository;
import org.chzz.market.domain.auction.repository.AuctionQueryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AuctionSearchService {
    private final AuctionQueryRepository auctionQueryRepository;
    private final AuctionElasticQueryRepository auctionElasticQueryRepository;

    /**
     * 경매 검색
     */
    public Page<?> search(Long userId, String keyword, AuctionStatus status, Pageable pageable) {
        SearchHits<AuctionDocument> searchHits = auctionElasticQueryRepository.searchAuctions(keyword, status,
                pageable);
        List<AuctionDocument> auctionDocuments = searchHits.getSearchHits().stream().map(hit -> hit.getContent())
                .toList();

        if (auctionDocuments.isEmpty()) {
            return Page.empty(pageable);
        }

        // 상태에 따라 응답 생성
        List<?> responses = createResponsesByStatus(userId, status, auctionDocuments);
        return new PageImpl<>(responses, pageable, searchHits.getTotalHits());
    }

    /**
     * 상태에 따라 적절한 응답 생성
     */
    private List<?> createResponsesByStatus(Long userId, AuctionStatus status, List<AuctionDocument> auctionDocuments) {
        List<Long> auctionIds = auctionDocuments.stream()
                .map(AuctionDocument::getAuctionId)
                .toList();

        if (status.equals(AuctionStatus.PRE)) {
            return createPreAuctionResponses(userId, auctionDocuments, auctionIds);
        } else {
            return createOfficialAuctionResponses(userId, auctionDocuments, auctionIds);
        }
    }

    /**
     * 사전 경매 응답 생성
     */
    private List<PreAuctionResponse> createPreAuctionResponses(Long userId,
                                                               List<AuctionDocument> auctionDocuments,
                                                               List<Long> auctionIds) {
        Map<Long, AuctionLikeDetail> auctionLikeDetails = auctionQueryRepository.findAuctionLikeDetailsByAuctionIds(
                        auctionIds, userId).stream()
                .collect(Collectors.toMap(AuctionLikeDetail::auctionId, dto -> dto));

        return auctionDocuments.stream().map(auctionDocument -> {
            AuctionLikeDetail auctionLikeDetail = auctionLikeDetails.getOrDefault(auctionDocument.getAuctionId(),
                    new AuctionLikeDetail(userId, 0L, false));
            return new PreAuctionResponse(auctionDocument, auctionLikeDetail, userId);
        }).toList();
    }

    /**
     * 공식 경매 응답 생성
     */
    private List<OfficialAuctionResponse> createOfficialAuctionResponses(Long userId,
                                                                         List<AuctionDocument> auctionDocuments,
                                                                         List<Long> auctionIds) {
        Map<Long, AuctionBidDetail> auctionBidDetails = auctionQueryRepository.findAuctionBidDetailsByAuctionIds(
                auctionIds, userId).stream().collect(Collectors.toMap(AuctionBidDetail::auctionId, dto -> dto));

        return auctionDocuments.stream().map(auctionDocument -> {
            AuctionBidDetail auctionBidDetail = auctionBidDetails.getOrDefault(auctionDocument.getAuctionId(),
                    new AuctionBidDetail(userId, 0L, false));
            return new OfficialAuctionResponse(auctionDocument, auctionBidDetail, userId);
        }).toList();
    }
}
