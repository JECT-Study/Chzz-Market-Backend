package org.chzz.market.domain.bid.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.chzz.market.domain.auction.type.AuctionStatus;
import org.chzz.market.domain.bid.dto.BidCreateRequest;
import org.chzz.market.domain.bid.dto.query.BiddingRecord;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

@Tag(name = "bids", description = "입찰 API")
public interface BidApi {
    @Operation(summary = "나의 입찰 목록 조회")
    ResponseEntity<Page<BiddingRecord>> findUsersBidHistory(Long userId, @ParameterObject Pageable pageable, AuctionStatus status);

    @Operation(summary = "입찰 요청 및 수정")
    ResponseEntity<Void> createBid(BidCreateRequest bidCreateRequest, Long userId);

    @Operation(summary = "입찰 취소")
    ResponseEntity<Void> cancelBid(Long bidId, Long userId);
}
