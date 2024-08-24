package org.chzz.market.domain.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.chzz.market.domain.auction.dto.response.MyAuctionResponse;
import org.chzz.market.domain.auction.service.AuctionService;
import org.chzz.market.domain.user.dto.UpdateProfileResponse;
import org.chzz.market.domain.user.dto.UpdateUserProfileRequest;
import org.chzz.market.domain.user.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users/me")
public class MyController {
    private final AuctionService auctionService;
    private final UserService userService;

    @GetMapping("/auctions")
    public ResponseEntity<Page<MyAuctionResponse>> getMyAuctionList(@PageableDefault(sort = "newest") Pageable pageable) {
        return ResponseEntity.ok(auctionService.getAuctionListByUserId(1L, pageable)); // TODO: 추후에 인증된 사용자 정보로 수정 필요
    }

    /**
     * 사용자 프로필 수정
     */
    @PostMapping("/{nickname}")
    public ResponseEntity<UpdateProfileResponse> updateUserProfile(
            @PathVariable String nickname,
            @RequestBody @Valid UpdateUserProfileRequest request) {
        UpdateProfileResponse response = userService.updateUserProfile(nickname, request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
