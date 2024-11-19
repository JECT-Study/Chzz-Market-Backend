package org.chzz.market.domain.bid.controller;

import static org.chzz.market.domain.auctionv2.error.AuctionErrorCode.Const.AUCTION_ENDED;
import static org.chzz.market.domain.auctionv2.error.AuctionErrorCode.Const.AUCTION_NOT_FOUND;
import static org.chzz.market.domain.bid.error.BidErrorCode.Const.BID_ALREADY_CANCELLED;
import static org.chzz.market.domain.bid.error.BidErrorCode.Const.BID_BELOW_MIN_PRICE;
import static org.chzz.market.domain.bid.error.BidErrorCode.Const.BID_BY_OWNER;
import static org.chzz.market.domain.bid.error.BidErrorCode.Const.BID_LIMIT_EXCEEDED;
import static org.chzz.market.domain.bid.error.BidErrorCode.Const.BID_NOT_FOUND;
import static org.chzz.market.domain.bid.error.BidErrorCode.Const.BID_SAME_AS_PREVIOUS;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.chzz.market.common.config.LoginUser;
import org.chzz.market.common.springdoc.ApiExceptionExplanation;
import org.chzz.market.common.springdoc.ApiResponseExplanations;
import org.chzz.market.domain.auctionv2.entity.AuctionStatus;
import org.chzz.market.domain.auctionv2.error.AuctionErrorCode;
import org.chzz.market.domain.bid.dto.BidCreateRequest;
import org.chzz.market.domain.bid.dto.query.BiddingRecord;
import org.chzz.market.domain.bid.error.BidErrorCode;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "bids", description = "입찰 API")
public interface BidApi {
    @Operation(summary = "나의 입찰 목록 조회")
    ResponseEntity<Page<BiddingRecord>> findUsersBidHistory(@LoginUser Long userId,
                                                            @PageableDefault(sort = "time-remaining") @ParameterObject Pageable pageable,
                                                            @RequestParam(value = "status", required = false) AuctionStatus status);

    @Operation(summary = "입찰 요청 및 수정")
    @ApiResponseExplanations(
            errors = {
                    @ApiExceptionExplanation(value = BidErrorCode.class, constant = BID_LIMIT_EXCEEDED, name = "입찰 횟수 제한을 초과 했을때"),
                    @ApiExceptionExplanation(value = AuctionErrorCode.class, constant = AUCTION_ENDED, name = "해당 경매가 진행 중이 아니거나 이미 종료되었습니다."),
                    @ApiExceptionExplanation(value = BidErrorCode.class, constant = BID_SAME_AS_PREVIOUS, name = "이전 입찰금액과 동일한 경우"),
                    @ApiExceptionExplanation(value = BidErrorCode.class, constant = BID_BELOW_MIN_PRICE, name = "입찰금액이 최소가보다 낮은 경우"),
                    @ApiExceptionExplanation(value = BidErrorCode.class, constant = BID_BY_OWNER, name = "경매 등록자가 입찰 할때"),
                    @ApiExceptionExplanation(value = BidErrorCode.class, constant = BID_NOT_FOUND, name = "없는 입찰 일때"),
                    @ApiExceptionExplanation(value = AuctionErrorCode.class, constant = AUCTION_NOT_FOUND, name = "없는 경매 일때"),
                    @ApiExceptionExplanation(value = BidErrorCode.class, constant = BID_ALREADY_CANCELLED, name = "취소한 입찰 일때"),
            }
    )
    ResponseEntity<Void> createBid(@Valid @RequestBody BidCreateRequest bidCreateRequest,
                                   @LoginUser Long userId);

    @Operation(summary = "입찰 취소")
    @ApiResponseExplanations(
            errors = {
                    @ApiExceptionExplanation(value = AuctionErrorCode.class, constant = AUCTION_ENDED, name = "해당 경매가 진행 중이 아니거나 이미 종료되었습니다."),
                    @ApiExceptionExplanation(value = BidErrorCode.class, constant = BID_BY_OWNER, name = "경매 등록자가 입찰취소 할때"),
                    @ApiExceptionExplanation(value = BidErrorCode.class, constant = BID_NOT_FOUND, name = "없는 입찰 일때"),
                    @ApiExceptionExplanation(value = AuctionErrorCode.class, constant = AUCTION_NOT_FOUND, name = "없는 경매 일때"),
                    @ApiExceptionExplanation(value = BidErrorCode.class, constant = BID_ALREADY_CANCELLED, name = "취소한 입찰 일때"),
            }
    )
    ResponseEntity<Void> cancelBid(Long bidId, Long userId);
}
