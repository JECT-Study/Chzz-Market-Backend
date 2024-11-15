package org.chzz.market.domain.auction.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.chzz.market.common.config.LoginUser;
import org.chzz.market.domain.auction.dto.request.BaseRegisterRequest;
import org.chzz.market.domain.auction.dto.request.StartAuctionRequest;
import org.chzz.market.domain.auction.dto.response.AuctionDetailsResponse;
import org.chzz.market.domain.auction.dto.response.AuctionResponse;
import org.chzz.market.domain.auction.dto.response.LostAuctionResponse;
import org.chzz.market.domain.auction.dto.response.RegisterResponse;
import org.chzz.market.domain.auction.dto.response.StartAuctionResponse;
import org.chzz.market.domain.auction.dto.response.UserAuctionResponse;
import org.chzz.market.domain.auction.dto.response.UserEndedAuctionResponse;
import org.chzz.market.domain.auction.dto.response.WonAuctionDetailsResponse;
import org.chzz.market.domain.auction.dto.response.WonAuctionResponse;
import org.chzz.market.domain.auction.service.AuctionRegistrationServiceFactory;
import org.chzz.market.domain.auction.service.AuctionService;
import org.chzz.market.domain.auction.service.register.AuctionRegistrationService;
import org.chzz.market.domain.auction.type.TestService;
import org.chzz.market.domain.bid.dto.response.BidInfoResponse;
import org.chzz.market.domain.bid.service.BidService;
import org.chzz.market.domain.product.entity.Product.Category;
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

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/auctions")
public class AuctionController implements AuctionApi {
    private final AuctionService auctionService;
    private final BidService bidService;
    private final TestService testService;
    private final AuctionRegistrationServiceFactory registrationServiceFactory;

    /**
     * 경매 목록 조회
     */
    @Override
    @GetMapping
    public ResponseEntity<Page<AuctionResponse>> getAuctionList(@RequestParam Category category,
                                                                @LoginUser Long userId,
                                                                Pageable pageable) {
        return ResponseEntity.ok(auctionService.getAuctionListByCategory(category, userId, pageable));
    }

    /**
     * Best 경매 상품 목록 조회
     */
    @Override
    @GetMapping("/best")
    public ResponseEntity<List<AuctionResponse>> bestAuctionList() {
        List<AuctionResponse> bestAuctionList = auctionService.getBestAuctionList();
        return ResponseEntity.ok(bestAuctionList);
    }

    /**
     * Imminent 경매 상품 목록 조회
     */
    @Override
    @GetMapping("/imminent")
    public ResponseEntity<List<AuctionResponse>> imminentAuctionList() {
        List<AuctionResponse> imminentAuctionList = auctionService.getImminentAuctionList();
        return ResponseEntity.ok(imminentAuctionList);
    }

    /**
     * 내가 성공한 경매 조회
     */
    @Override
    @GetMapping("/won")
    public ResponseEntity<Page<WonAuctionResponse>> getWonAuctionHistory(
            @LoginUser Long userId,
            @PageableDefault(size = 20, sort = "newest") Pageable pageable) {
        return ResponseEntity.ok(auctionService.getWonAuctionHistory(userId, pageable));
    }

    /**
     * 내가 실패한 경매 조회
     */
    @Override
    @GetMapping("/lost")
    public ResponseEntity<Page<LostAuctionResponse>> getLostAuctionHistory(
            @LoginUser Long userId,
            @PageableDefault(size = 20, sort = "newest") Pageable pageable) {
        return ResponseEntity.ok(auctionService.getLostAuctionHistory(userId, pageable));
    }

    /**
     * 경매 상세 조회
     */
    @Override
    @GetMapping("/{auctionId}")
    public ResponseEntity<AuctionDetailsResponse> getAuctionDetails(
            @PathVariable Long auctionId,
            @LoginUser Long userId) {
        return ResponseEntity.ok(auctionService.getFullAuctionDetails(auctionId, userId));
    }

    /**
     * 경매 입찰 목록 조회
     */
    @Override
    @GetMapping("/{auctionId}/bids")
    public ResponseEntity<Page<BidInfoResponse>> getBids(@LoginUser Long userId, @PathVariable Long auctionId,
                                                         Pageable pageable) {
        return ResponseEntity.ok(bidService.getBidsByAuctionId(userId, auctionId, pageable));
    }

    /**
     * 낙찰 정보 조회
     */
    @Override
    @GetMapping("/{auctionId}/winning-bid")
    public ResponseEntity<WonAuctionDetailsResponse> getWinningBid(@LoginUser Long userId,
                                                                   @PathVariable Long auctionId) {
        return ResponseEntity.ok(auctionService.getWinningBidByAuctionId(userId, auctionId));
    }

    /**
     * 사용자가 등록한 모든 경매 목록 조회 현재 사용 X
     */
    @Override
    @GetMapping("/users")
    public ResponseEntity<Page<UserAuctionResponse>> getUserRegisteredAuction(@LoginUser Long userId,
                                                                              Pageable pageable) {
        return ResponseEntity.ok(auctionService.getAuctionListByUserId(userId, pageable));
    }

    /**
     * 사용자 경매 상품 목록 조회 (닉네임) 현재 사용 X
     */
    @Override
    @GetMapping("/users/{nickname}")
    public ResponseEntity<Page<UserAuctionResponse>> getUserAuctionList(@PathVariable String nickname,
                                                                        @PageableDefault(sort = "newest") Pageable pageable) {
        return ResponseEntity.ok(auctionService.getAuctionListByNickname(nickname, pageable));
    }

    /**
     * 사용자의 진행 중인 경매 목록 조회
     */
    @Override
    @GetMapping("/users/proceeding")
    public ResponseEntity<Page<UserAuctionResponse>> getProceedingAuctions(@LoginUser Long userId,
                                                                           @PageableDefault(sort = "newest") Pageable pageable) {
        return ResponseEntity.ok(auctionService.getProceedingAuctionListByUserId(userId, pageable));
    }

    /**
     * 사용자의 종료된 경매 목록 조회
     */
    @Override
    @GetMapping("/users/ended")
    public ResponseEntity<Page<UserEndedAuctionResponse>> getEndedAuctions(@LoginUser Long userId,
                                                                           @PageableDefault(sort = "newest") Pageable pageable) {
        return ResponseEntity.ok(auctionService.getEndedAuctionListByUserId(userId, pageable));
    }

    /**
     * 경매 등록
     */
    @Override
    @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<RegisterResponse> registerAuction(
            @LoginUser Long userId,
            @RequestPart("request") @Valid BaseRegisterRequest request,
            @RequestPart(value = "images") List<MultipartFile> images) {

        AuctionRegistrationService auctionRegistrationService = registrationServiceFactory.getService(
                request.getAuctionRegisterType());
        RegisterResponse response = auctionRegistrationService.register(userId, request, images);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 경매 상품으로 전환
     */
    @Override
    @PostMapping("/start")
    public ResponseEntity<StartAuctionResponse> startAuction(@LoginUser Long userId,
                                                             @RequestBody @Valid StartAuctionRequest request) {
        StartAuctionResponse response = auctionService.startAuction(userId, request);
        log.info("경매 상품으로 성공적으로 전환되었습니다. 상품 ID: {}", response.productId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

//    ---------------------------------------------------------------------------------------

    /**
     * 경매 종료 테스트 API (삭제 필요)
     */
    @Override
    @PostMapping("/test")
    public ResponseEntity<Void> testEndAuction(@LoginUser Long userId,
                                               @RequestParam("seconds") int seconds) {
        testService.test(userId, seconds);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
