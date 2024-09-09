package org.chzz.market.domain.auction.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.chzz.market.common.config.LoginUser;
import org.chzz.market.domain.auction.dto.request.BaseRegisterRequest;
import org.chzz.market.domain.auction.dto.request.StartAuctionRequest;
import org.chzz.market.domain.auction.dto.response.*;
import org.chzz.market.domain.auction.service.AuctionRegistrationServiceFactory;
import org.chzz.market.domain.auction.service.AuctionService;
import org.chzz.market.domain.auction.service.register.AuctionRegistrationService;
import org.chzz.market.domain.bid.service.BidService;
import org.chzz.market.domain.product.entity.Product.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@RestController
@RequestMapping("/auctions")
public class AuctionController {
    private static final Logger logger = LoggerFactory.getLogger(AuctionController.class);

    private final AuctionService auctionService;
    private final BidService bidService;
    private final AuctionRegistrationServiceFactory registrationServiceFactory;

    @GetMapping
    public ResponseEntity<?> getAuctionList(@RequestParam Category category,
                                            @LoginUser Long userId,
                                            Pageable pageable) {
        return ResponseEntity.ok(auctionService.getAuctionListByCategory(category, userId, pageable));
    }

    @GetMapping("/{auctionId}")
    public ResponseEntity<?> getAuctionDetails(@PathVariable Long auctionId, @LoginUser Long userId) {
        return ResponseEntity.ok(auctionService.getAuctionDetails(auctionId, userId));
    }

    @GetMapping("/history")
    public ResponseEntity<?> getAuctionHistory(@LoginUser Long userId, Pageable pageable) {
        return ResponseEntity.ok(auctionService.getAuctionHistory(userId, pageable));
    }

    /*
     * 내가 성공한 경매 조회
     */
    @GetMapping("/won")
    public ResponseEntity<Page<WonAuctionResponse>> getWonAuctionHistory(
            @LoginUser Long userId,
            @PageableDefault(size = 20, sort = "newest") Pageable pageable) {
        return ResponseEntity.ok(auctionService.getWonAuctionHistory(userId, pageable));
    }

    /*
     * 내가 실패한 경매 조회
     */
    @GetMapping("/lost")
    public ResponseEntity<Page<LostAuctionResponse>> getLostAuctionHistory(
            @LoginUser Long userId,
            @PageableDefault(size = 20, sort = "newest") Pageable pageable) {
        return ResponseEntity.ok(auctionService.getLostAuctionHistory(userId, pageable));
    }

    /**
     * 경매 등록
     */
    @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<RegisterResponse> registerAuction(
            @LoginUser Long userId,
            @RequestPart("request") @Valid BaseRegisterRequest request,
            @RequestPart(value = "images", required = true) List<MultipartFile> images) {

        AuctionRegistrationService auctionRegistrationService = registrationServiceFactory.getService(
                request.getAuctionRegisterType());
        RegisterResponse response = auctionRegistrationService.register(userId, request, images);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 경매 상품으로 전환
     */
    @PostMapping("/start")
    public ResponseEntity<StartAuctionResponse> startAuction(@LoginUser Long userId, @RequestBody @Valid StartAuctionRequest request) {
        StartAuctionResponse response = auctionService.startAuction(userId, request);
        logger.info("경매 상품으로 성공적으로 전환되었습니다. 상품 ID: {}", response.productId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /*
     * 사용자 경매 상품 목록 조회
     */
    @GetMapping("/users/{nickname}")
    public ResponseEntity<Page<UserAuctionResponse>> getUserAuctionList(@PathVariable String nickname,
                                                                        @PageableDefault(sort = "newest") Pageable pageable) {
        return ResponseEntity.ok(auctionService.getAuctionListByNickname(nickname, pageable));
    }

    /*
     * Best 경매 상품 목록 조회
     */
    @GetMapping("/best")
    public ResponseEntity<?> bestAuctionList() {
        List<AuctionResponse> bestAuctionList=auctionService.getBestAuctionList();
        return ResponseEntity.ok(bestAuctionList);
    }

    @GetMapping("/imminent")
    public ResponseEntity<?> imminentAuctionList() {
        List<AuctionResponse> imminentAuctionList = auctionService.getImminentAuctionList();
        return ResponseEntity.ok(imminentAuctionList);
    }

    @GetMapping("/{auctionId}/bids")
    public ResponseEntity<?> getBids(@LoginUser Long userId, @PathVariable Long auctionId, Pageable pageable) {
        return ResponseEntity.ok(bidService.getBidsByAuctionId(userId, auctionId, pageable));
    }
}
