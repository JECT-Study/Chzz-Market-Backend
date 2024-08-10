package org.chzz.market.domain.bid.controller;

import static org.springframework.http.HttpStatus.CREATED;

import lombok.RequiredArgsConstructor;
import org.chzz.market.domain.bid.dto.BidCreateRequest;
import org.chzz.market.domain.bid.service.BidService;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<?> createBid(@RequestBody BidCreateRequest bidCreateRequest) {
//                                       @AuthenticationPrincipal CustomUserDetails customUserDetails) // TODO: 추후에 인증된 사용자 정보로 수정 필요
        bidService.createBid(bidCreateRequest, 1L);
        return ResponseEntity.status(CREATED).build();
    }

    @PatchMapping("/{bidId}/cancel")
    public ResponseEntity<?> cancelBid(@PathVariable Long bidId) {
        //                                       @AuthenticationPrincipal CustomUserDetails customUserDetails) // TODO: 추후에 인증된 사용자 정보로 수정 필요
        bidService.cancelBid(bidId, 2L); // TODO: 추후에 인증된 사용자 정보로 수정 필요
        return ResponseEntity.ok().build();
    }
}
