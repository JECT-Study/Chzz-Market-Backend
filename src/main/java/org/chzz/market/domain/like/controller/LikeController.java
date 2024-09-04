package org.chzz.market.domain.like.controller;

import lombok.RequiredArgsConstructor;
import org.chzz.market.domain.like.service.LikeService;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/likes")
public class LikeController {

    private final LikeService likeService;
}