package org.chzz.market.domain.bid.controller;

import static org.springframework.http.HttpStatus.CREATED;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.chzz.market.common.config.LoginUser;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/bids")
public class BidController {
    private final BidService bidService;

    @PostMapping
    public ResponseEntity<?> createBid(@Valid @RequestBody BidCreateRequest bidCreateRequest,
                                       @LoginUser Long userId) {
        bidService.createBid(bidCreateRequest, userId);
        return ResponseEntity.status(CREATED).build();
    }

    @GetMapping
    public ResponseEntity<?> findUsersBidHistory(
            @LoginUser Long userId,
            @PageableDefault
            Pageable pageable) {
        Page<BiddingRecord> records = bidService.inquireBidHistory(userId, pageable);
        return ResponseEntity.ok(records);
    }

    @PatchMapping("/{bidId}/cancel")
    public ResponseEntity<?> cancelBid(@PathVariable Long bidId,
                                       @LoginUser Long userId) {
        bidService.cancelBid(bidId, userId);
        return ResponseEntity.ok().build();
    }
}
