package org.chzz.market.domain.bid.controller;

import lombok.RequiredArgsConstructor;
import org.chzz.market.domain.bid.dto.query.BiddingRecord;
import org.chzz.market.domain.bid.service.BidService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/bids")
@RequiredArgsConstructor
public class BidController {
    private final BidService bidService;

    @GetMapping
    public ResponseEntity<?> findUsersBidHistory(
//            @AuthenticationPrincipal UserDetailsImpl UserDetailsImpl,
//            @RequestParam
            @PageableDefault
            Pageable pageable) {
        Page<BiddingRecord> records = bidService.inquireBidHistory(pageable);
        return ResponseEntity.ok(records);
    }
}
