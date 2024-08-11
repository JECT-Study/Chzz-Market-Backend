package org.chzz.market.domain.auction.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.chzz.market.domain.auction.dto.RegisterRequest;
import org.chzz.market.domain.auction.dto.RegisterResponse;
import org.chzz.market.domain.auction.dto.StartResponse;
import org.chzz.market.domain.auction.service.AuctionService;
import org.chzz.market.domain.product.entity.Product.Category;
import org.chzz.market.domain.auction.entity.SortType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/auctions")
public class AuctionController {
    private static final Logger logger = LoggerFactory.getLogger(AuctionController.class);

    private final AuctionService auctionService;

    @GetMapping
    public ResponseEntity<?> getAuctionList(@RequestParam Category category,
//                                            @AuthenticationPrincipal CustomUserDetails customUserDetails, // TODO: 추후에 인증된 사용자 정보로 수정 필요
                                            @RequestParam(defaultValue = "newest") SortType type,
                                            Pageable pageable) {
        return ResponseEntity.ok(auctionService.getAuctionListByCategory(category, type, 1L, pageable)); // 임의의 사용자 ID
    }

    @GetMapping("/{auctionId}")
    public ResponseEntity<?> getAuctionDetails(@PathVariable Long auctionId) {
        return ResponseEntity.ok(auctionService.getAuctionDetails(auctionId, 1L)); // TODO: 추후에 인증된 사용자 정보로 수정 필요
    }

    /**
     * 상품 등록
     */
    @PostMapping(value = "/register",
            consumes = {MediaType.MULTIPART_FORM_DATA_VALUE},
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<RegisterResponse> createAuction(@ModelAttribute @Valid RegisterRequest request) {

        RegisterResponse response = auctionService.register(request);
        logger.info("상품이 성공적으로 등록되었습니다. 상품 ID: {}", response.productId());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 경매 상품으로 전환
     */
    @PostMapping("/{auctionId}/start")
    public ResponseEntity<StartResponse> startAuction(@PathVariable Long auctionId, Long userId) {
        StartResponse response = auctionService.startAuction(auctionId, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
