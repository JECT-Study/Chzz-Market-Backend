package org.chzz.market.domain.auction.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.chzz.market.domain.auction.dto.request.BaseRegisterRequest;
import org.chzz.market.domain.auction.dto.request.StartAuctionRequest;
import org.chzz.market.domain.auction.dto.response.AuctionResponse;
import org.chzz.market.domain.auction.dto.response.RegisterResponse;
import org.chzz.market.domain.auction.dto.response.StartAuctionResponse;
import org.chzz.market.domain.auction.dto.response.UserAuctionResponse;
import org.chzz.market.domain.auction.service.AuctionRegistrationServiceFactory;
import org.chzz.market.domain.auction.service.AuctionService;
import org.chzz.market.domain.auction.service.register.AuctionRegistrationService;
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
    private final AuctionRegistrationServiceFactory registrationServiceFactory;

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


    @GetMapping("/history")
    public ResponseEntity<?> getAuctionHistory(
            //                                            @AuthenticationPrincipal CustomUserDetails customUserDetails, // TODO: 추후에 인증된 사용자 정보로 수정 필요
            Pageable pageable
    ) {
        return ResponseEntity.ok(auctionService.getAuctionHistory(1L, pageable));
    }

    /**
     * 상품 등록
     */
    @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<RegisterResponse> registerAuction(
            @RequestPart("request") @Valid BaseRegisterRequest request,
            @RequestPart(value = "images", required = true) List<MultipartFile> images) {

        AuctionRegistrationService auctionRegistrationService = registrationServiceFactory.getService(
                request.getAuctionRegisterType());
        RegisterResponse response = auctionRegistrationService.register(request, images);

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

    @GetMapping("/users/{nickname}")
    public ResponseEntity<Page<UserAuctionResponse>> getUserAuctionList(@PathVariable String nickname,
                                                                        @PageableDefault(sort = "newest") Pageable pageable) {
        return ResponseEntity.ok(auctionService.getAuctionListByNickname(nickname, pageable));
    }

    @GetMapping("/best")
    public ResponseEntity<?> bestAuctionList() {
        List<AuctionResponse> bestAuctionList=auctionService.getBestAuctionList(1L);//TODO 2024 08 26 13:59:54 : 인증된 사용자 정보로 수정
        return ResponseEntity.ok(bestAuctionList);
    }
}
