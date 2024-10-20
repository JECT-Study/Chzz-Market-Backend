package org.chzz.market.domain.bid.controller;

import static org.chzz.market.domain.auction.error.AuctionErrorCode.Const.AUCTION_ENDED;
import static org.chzz.market.domain.bid.error.BidErrorCode.Const.BID_ALREADY_CANCELLED;
import static org.chzz.market.domain.bid.error.BidErrorCode.Const.BID_BELOW_MIN_PRICE;
import static org.chzz.market.domain.bid.error.BidErrorCode.Const.BID_BY_OWNER;
import static org.chzz.market.domain.bid.error.BidErrorCode.Const.BID_LIMIT_EXCEEDED;
import static org.chzz.market.domain.bid.error.BidErrorCode.Const.BID_NOT_FOUND;
import static org.chzz.market.domain.bid.error.BidErrorCode.Const.BID_SAME_AS_PREVIOUS;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.chzz.market.common.springdoc.ApiExceptionExplanation;
import org.chzz.market.common.springdoc.ApiResponseExplanations;
import org.chzz.market.domain.auction.error.AuctionErrorCode;
import org.chzz.market.domain.auction.type.AuctionStatus;
import org.chzz.market.domain.bid.dto.BidCreateRequest;
import org.chzz.market.domain.bid.dto.query.BiddingRecord;
import org.chzz.market.domain.bid.error.BidErrorCode;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

@Tag(name = "bids", description = "입찰 API")
public interface BidApi {
    @Operation(summary = "나의 입찰 목록 조회")
    ResponseEntity<Page<BiddingRecord>> findUsersBidHistory(Long userId, @ParameterObject Pageable pageable,
                                                            AuctionStatus status);

    @Operation(summary = "입찰 요청 및 수정")
    @ApiResponseExplanations(
            errors = {
                    @ApiExceptionExplanation(value = BidErrorCode.class, constant = BID_LIMIT_EXCEEDED, name = "입찰 횟수 제한을 초과 했을때"),
                    @ApiExceptionExplanation(value = AuctionErrorCode.class, constant = AUCTION_ENDED, name = "경매가 종료된 경우"),
                    @ApiExceptionExplanation(value = BidErrorCode.class, constant = BID_SAME_AS_PREVIOUS, name = "이전 입찰금액과 동일한 경우"),
                    @ApiExceptionExplanation(value = BidErrorCode.class, constant = BID_BELOW_MIN_PRICE, name = "입찰금액이 최소가보다 낮은 경우"),
                    @ApiExceptionExplanation(value = BidErrorCode.class, constant = BID_BY_OWNER, name = "경매 등록자가 입찰 할때"),
                    @ApiExceptionExplanation(value = BidErrorCode.class, constant = BID_NOT_FOUND, name = "없는 경매 일때"),
                    @ApiExceptionExplanation(value = BidErrorCode.class, constant = BID_NOT_FOUND, name = "없는 경매 일때"),
                    @ApiExceptionExplanation(value = BidErrorCode.class, constant = BID_ALREADY_CANCELLED, name = "취소한 입찰 일때"),
            }
    )
    ResponseEntity<Void> createBid(BidCreateRequest bidCreateRequest, Long userId);

    @Operation(summary = "입찰 취소")
    ResponseEntity<Void> cancelBid(Long bidId, Long userId);
}
