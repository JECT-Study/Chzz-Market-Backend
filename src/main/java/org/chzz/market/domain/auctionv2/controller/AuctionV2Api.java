package org.chzz.market.domain.auctionv2.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.chzz.market.common.config.LoginUser;
import org.chzz.market.domain.auction.dto.request.BaseRegisterRequest;
import org.chzz.market.domain.auction.dto.response.RegisterResponse;
import org.chzz.market.domain.auctionv2.dto.response.CategoryResponse;
import org.chzz.market.domain.auctionv2.dto.view.AuctionType;
import org.chzz.market.domain.auctionv2.dto.view.UserAuctionType;
import org.chzz.market.domain.auctionv2.entity.Category;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "auctions(v2)", description = "V2 경매 API")
@RequestMapping("/v2/auctions")
public interface AuctionV2Api {
    @Operation(summary = "경매 목록 조회", description = "경매 목록을 조회합니다. type 파라미터를 통해 조회 유형을 지정합니다.")
    @GetMapping
    ResponseEntity<Page<?>> getAuctionList(@LoginUser Long userId, @RequestParam(required = false) Category category,
                                           @RequestParam AuctionType type,
                                           @ParameterObject @PageableDefault(sort = "newest") Pageable pageable);

    @Operation(summary = "경매 카테고리 조회", description = "경매 카테고리 목록을 조회합니다.")
    @GetMapping("/categories")
    ResponseEntity<List<CategoryResponse>> getCategoryList();

    @Operation(summary = "사용자 경매 목록 조회", description = "사용자가 등록한 경매 목록을 조회합니다. type 파라미터를 통해 조회 유형을 지정합니다.")
    @GetMapping("/users")
    ResponseEntity<Page<?>> getUserAuctionList(@LoginUser Long userId, @RequestParam UserAuctionType type,
                                               @ParameterObject @PageableDefault(sort = "newest") Pageable pageable);

    @Operation(summary = "경매 등록", description = "경매를 등록합니다.")
    @PostMapping
    ResponseEntity<RegisterResponse> registerAuction(@LoginUser Long userId,
                                                     @RequestPart("request") @Valid BaseRegisterRequest request,
                                                     @RequestPart(value = "images") List<MultipartFile> images);

    @Operation(summary = "경매 테스트 등록", description = "테스트 등록합니다.")
    @PostMapping("/test")
    ResponseEntity<Void> testEndAuction(@LoginUser Long userId,
                                               @RequestParam int seconds);
}
