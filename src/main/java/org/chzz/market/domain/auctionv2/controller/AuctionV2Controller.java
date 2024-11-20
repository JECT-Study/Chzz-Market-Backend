package org.chzz.market.domain.auctionv2.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.chzz.market.common.config.LoginUser;
import org.chzz.market.domain.auctionv2.dto.AuctionRegisterType;
import org.chzz.market.domain.auctionv2.dto.request.RegisterRequest;
import org.chzz.market.domain.auctionv2.dto.response.CategoryResponse;
import org.chzz.market.domain.auctionv2.dto.view.AuctionType;
import org.chzz.market.domain.auctionv2.dto.view.UserAuctionType;
import org.chzz.market.domain.auctionv2.entity.Category;
import org.chzz.market.domain.auctionv2.service.AuctionCategoryService;
import org.chzz.market.domain.auctionv2.service.AuctionTestService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
public class AuctionV2Controller implements AuctionV2Api {
    private final AuctionCategoryService auctionCategoryService;
    private final AuctionTestService testService;

    @Override
    @GetMapping
    public ResponseEntity<Page<?>> getAuctionList(@LoginUser Long userId,
                                                  @RequestParam(required = false) Category category,
                                                  @RequestParam AuctionType type,
                                                  @PageableDefault(sort = "newest") Pageable pageable) {
        return null;
    }

    @Override
    @GetMapping("/categories")
    public ResponseEntity<List<CategoryResponse>> getCategoryList() {
        return ResponseEntity.ok(auctionCategoryService.getCategories());
    }

    @Override
    @GetMapping("/users")
    public ResponseEntity<Page<?>> getUserAuctionList(@LoginUser Long userId,
                                                      @RequestParam UserAuctionType type,
                                                      @PageableDefault(sort = "newest") Pageable pageable) {
        return null;
    }

    @Override
    @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Void> registerAuction(
            @LoginUser Long userId,
            @RequestPart("request") @Valid RegisterRequest request,
            @RequestPart(value = "images") List<MultipartFile> images) {
        AuctionRegisterType type = request.auctionRegisterType();
        type.getService().register(userId, request, images);//요청 타입에 따라 다른 서비스 호출
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Override
    @PostMapping("/test")
    public ResponseEntity<Void> testEndAuction(@LoginUser Long userId,
                                               @RequestParam("seconds") int seconds) {
        testService.test(userId, seconds);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
