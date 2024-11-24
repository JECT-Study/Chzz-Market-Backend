package org.chzz.market.domain.auctionv2.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.chzz.market.common.config.LoginUser;
import org.chzz.market.common.validation.annotation.NotEmptyMultipartList;
import org.chzz.market.domain.auctionv2.dto.AuctionRegisterType;
import org.chzz.market.domain.auctionv2.dto.request.RegisterRequest;
import org.chzz.market.domain.auctionv2.dto.response.CategoryResponse;
import org.chzz.market.domain.auctionv2.dto.response.PreAuctionResponse;
import org.chzz.market.domain.auctionv2.entity.AuctionStatus;
import org.chzz.market.domain.auctionv2.entity.Category;
import org.chzz.market.domain.auctionv2.service.AuctionCategoryService;
import org.chzz.market.domain.auctionv2.service.AuctionLookupService;
import org.chzz.market.domain.auctionv2.service.AuctionMyService;
import org.chzz.market.domain.auctionv2.service.AuctionTestService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@Validated
public class AuctionV2Controller implements AuctionV2Api {
    private final AuctionLookupService auctionLookupService;
    private final AuctionCategoryService auctionCategoryService;
    private final AuctionTestService testService;
    private final AuctionMyService auctionMyService;

    /**
     * 경매 목록 조회
     */
    @Override
    @GetMapping
    public ResponseEntity<Page<?>> getAuctionList(@LoginUser Long userId,
                                                  @RequestParam(required = false) Category category,
                                                  @RequestParam(required = false, defaultValue = "proceeding") AuctionStatus status,
                                                  @RequestParam(required = false) @Min(value = 1, message = "minutes는 1 이상의 값이어야 합니다.") Integer minutes,
                                                  @PageableDefault(sort = "newest-v2") Pageable pageable) {
        return ResponseEntity.ok(
                auctionLookupService.getAuctionList(userId, category, status, minutes, pageable));
    }

    /**
     * 경매 카테고리 Enum 조회
     */
    @Override
    @GetMapping("/categories")
    public ResponseEntity<List<CategoryResponse>> getCategoryList() {
        return ResponseEntity.ok(auctionCategoryService.getCategories());
    }

    /**
     * 사용자가 등록한 진행중인 경매 목록 조회
     */
    @Override
    @GetMapping("/users/proceeding")
    public ResponseEntity<Page<?>> getUserProceedingAuctionList(@LoginUser Long userId,
                                                                @PageableDefault(sort = "newest") Pageable pageable) {
        return null;
    }

    /**
     * 사용자가 등록한 종료된 경매 목록 조회
     */
    @Override
    public ResponseEntity<Page<?>> getUserEndedAuctionList(@LoginUser Long userId,
                                                           @PageableDefault(sort = "newest") Pageable pageable) {
        return null;
    }

    /**
     * 사용자가 등록한 사전 경매 목록 조회
     */
    @Override
    @GetMapping("/users/pre")
    public ResponseEntity<Page<PreAuctionResponse>> getUserPreAuctionList(@LoginUser Long userId,
                                                                          @PageableDefault(sort = "newest-v2") Pageable pageable) {
        return ResponseEntity.ok(auctionMyService.getUserPreAuctionList(userId, pageable));
    }

    /**
     * 사용자가 낙찰한 경매 목록 조회
     */
    @Override
    public ResponseEntity<Page<?>> getUserWonAuctionList(@LoginUser Long userId,
                                                         @PageableDefault(sort = "newest") Pageable pageable) {
        return null;
    }

    /**
     * 사용자가 낙찰실패한 경매 목록 조회
     */
    @Override
    public ResponseEntity<Page<?>> getUserLostAuctionList(@LoginUser Long userId,
                                                          @PageableDefault(sort = "newest") Pageable pageable) {
        return null;
    }

    /**
     * 사용자가 좋아요(찜)한 경매 목록 조회
     */
    @Override
    @GetMapping("/users/likes")
    public ResponseEntity<Page<PreAuctionResponse>> getLikedAuctionList(@LoginUser Long userId,
                                                                        @PageableDefault(sort = "newest-v2") Pageable pageable) {
        return ResponseEntity.ok(auctionMyService.getLikedAuctionList(userId, pageable));
    }

    /**
     * 경매 등록
     */
    @Override
    @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Void> registerAuction(@LoginUser
                                                Long userId,

                                                @RequestPart("request")
                                                @Valid
                                                RegisterRequest request,

                                                @RequestPart(value = "images")
                                                @Valid
                                                @NotEmptyMultipartList
                                                @Size(max = 5, message = "이미지는 5장 이내로만 업로드 가능합니다.")
                                                List<MultipartFile> images) {
        AuctionRegisterType type = request.auctionRegisterType();
        type.getService().register(userId, request, images);//요청 타입에 따라 다른 서비스 호출
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 경매 테스트 등록
     */
    @Override
    @PostMapping("/test")
    public ResponseEntity<Void> testEndAuction(@LoginUser Long userId,
                                               @RequestParam("seconds") int seconds) {
        testService.test(userId, seconds);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
