package org.chzz.market.domain.like.controller;

import lombok.RequiredArgsConstructor;
import org.chzz.market.domain.like.dto.LikeResponse;
import org.chzz.market.domain.like.service.LikeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/likes")
public class LikeController {

    private final LikeService likeService;

    @PostMapping("/{productId}")
    public ResponseEntity<LikeResponse> toggleProductLike(
            @PathVariable Long productId,
            @RequestHeader("X-User-Agent") Long userId) {
        LikeResponse response = likeService.toggleLike(userId, productId);
        return ResponseEntity.ok(response);
    }
}