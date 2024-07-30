package org.chzz.market.domain.bid.controller;

import static org.springframework.http.HttpStatus.CREATED;

import lombok.RequiredArgsConstructor;
import org.chzz.market.domain.bid.dto.BidCreateRequest;
import org.chzz.market.domain.bid.service.BidService;
import org.chzz.market.domain.user.entity.User;
import org.chzz.market.domain.user.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/bids")
public class BidController {
    private final BidService bidService;

    private final UserRepository userRepository; // TODO: temporary

    @PostMapping
    public ResponseEntity<?> createBid(@RequestBody BidCreateRequest bidCreateRequest) {
//                                       @AuthenticationPrincipal CustomUserDetails customUserDetails) // TODO: 추후에 인증된 사용자 정보로 수정 필요
        User user = userRepository.findById(1L).get(); // TODO: temporary
        bidService.createBid(bidCreateRequest, user);
        return ResponseEntity.status(CREATED).build();
    }
}
