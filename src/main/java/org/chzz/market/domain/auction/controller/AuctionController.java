package org.chzz.market.domain.auction.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.chzz.market.domain.auction.dto.request.BaseRegisterRequest;
import org.chzz.market.domain.auction.dto.response.RegisterAuctionResponse;
import org.chzz.market.domain.auction.service.AuctionService;
import org.chzz.market.domain.auction.dto.request.StartAuctionRequest;
import org.chzz.market.domain.auction.dto.response.StartAuctionResponse;
import org.chzz.market.domain.product.entity.Product.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/auctions")
public class AuctionController {
    private static final Logger logger = LoggerFactory.getLogger(AuctionController.class);

    private final AuctionService auctionService;

    @GetMapping
    public ResponseEntity<?> getAuctionList(@RequestParam Category category,
//                                            @AuthenticationPrincipal CustomUserDetails customUserDetails, // TODO: 추후에 인증된 사용자 정보로 수정 필요
                                            Pageable pageable) {
        return ResponseEntity.ok(auctionService.getAuctionListByCategory(category, 1L, pageable)); // 임의의 사용자 ID
    }

    @GetMapping("/{auctionId}")
    public ResponseEntity<?> getAuctionDetails(@PathVariable Long auctionId) {
        return ResponseEntity.ok(auctionService.getAuctionDetails(auctionId, 1L)); // TODO: 추후에 인증된 사용자 정보로 수정 필요
    }

    /**
     * 상품 등록
     */
    @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<RegisterAuctionResponse> registerAuction(
            @RequestPart("request") @Valid BaseRegisterRequest request,
            @RequestPart(value = "images", required = true) List<MultipartFile> images) {

        RegisterAuctionResponse response = auctionService.registerAuction(request, images);
        if (response.status() != null) {
            logger.info("상품이 성공적으로 경매 등록되었습니다. 상품 ID: {}, 경매 ID: {}", response.productId(), response.auctionId());
        } else {
            logger.info("상품이 성공적으로 사전 등록되었습니다. 상품 ID: {}", response.productId());
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 경매 상품으로 전환
     */
    @PostMapping("/start")
    public ResponseEntity<StartAuctionResponse> startAuction(@RequestBody @Valid StartAuctionRequest request) {
        StartAuctionResponse response = auctionService.startAuction(request);
        logger.info("경매 상품으로 성공적으로 전환되었습니다. 상품 ID: {}", response.productId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
