package org.chzz.market.domain.bid.controller;

import static org.springframework.http.HttpStatus.CREATED;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.chzz.market.common.config.LoginUser;
import org.chzz.market.domain.auction.type.AuctionStatus;
import org.chzz.market.domain.bid.dto.BidCreateRequest;
import org.chzz.market.domain.bid.dto.query.BiddingRecord;
import org.chzz.market.domain.bid.service.BidService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/bids")
public class BidController implements BidApi {
    private final BidService bidService;

    /**
     * 나의 입찰 목록 조회
     *
     * @param status 경매 상태 필터링 파라미터
     *               - 전체 조회: 값이 없거나 null인 경우
     *               - 진행 중인 경매의 입찰만 조회: 'proceeding' 값을 사용할 때
     *               - 종료된 경매의 입찰만 조회: 'ended' 값을 사용할 때
     */
    @Override
    @GetMapping
    public ResponseEntity<Page<BiddingRecord>> findUsersBidHistory(
            @LoginUser Long userId,
            @PageableDefault(sort = "time-remaining") Pageable pageable,
            @RequestParam(value = "status", required = false) AuctionStatus status) {
        Page<BiddingRecord> records = bidService.inquireBidHistory(userId, pageable, status);
        return ResponseEntity.ok(records);
    }

    /**
     * 입찰 요청 및 수정
     */
    @Override
    @PostMapping
    public ResponseEntity<Void> createBid(@Valid @RequestBody BidCreateRequest bidCreateRequest,
                                          @LoginUser Long userId) {
        bidService.createBid(bidCreateRequest, userId);
        return ResponseEntity.status(CREATED).build();
    }

    /**
     * 입찰 취소
     */
    @Override
    @PatchMapping("/{bidId}/cancel")
    public ResponseEntity<Void> cancelBid(@PathVariable Long bidId,
                                          @LoginUser Long userId) {
        bidService.cancelBid(bidId, userId);
        return ResponseEntity.ok().build();
    }
}
