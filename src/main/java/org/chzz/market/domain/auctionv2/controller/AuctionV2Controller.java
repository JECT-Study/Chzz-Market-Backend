package org.chzz.market.domain.auctionv2.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.chzz.market.common.config.LoginUser;
import org.chzz.market.domain.auction.dto.request.BaseRegisterRequest;
import org.chzz.market.domain.auction.dto.response.RegisterResponse;
import org.chzz.market.domain.auctionv2.dto.response.CategoryResponse;
import org.chzz.market.domain.auctionv2.dto.view.AuctionType;
import org.chzz.market.domain.auctionv2.dto.view.UserAuctionType;
import org.chzz.market.domain.auctionv2.entity.Category;
import org.chzz.market.domain.auctionv2.service.AuctionCategoryService;
import org.chzz.market.domain.auctionv2.service.AuctionTestService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
public class AuctionV2Controller implements AuctionV2Api {
    private final AuctionCategoryService auctionCategoryService;
    private final AuctionTestService testService;

    @Override
    public ResponseEntity<Page<?>> getAuctionList(Long userId, Category category, AuctionType type, Pageable pageable) {
        return null;
    }

    @Override
    public ResponseEntity<List<CategoryResponse>> getCategoryList() {
        return ResponseEntity.ok(auctionCategoryService.getCategories());
    }

    @Override
    public ResponseEntity<Page<?>> getUserAuctionList(Long userId, UserAuctionType type, Pageable pageable) {
        return null;
    }

    @Override
    public ResponseEntity<RegisterResponse> registerAuction(Long userId, BaseRegisterRequest request,
                                                            List<MultipartFile> images) {
        return null;
    }

    @Override
    @PostMapping("/test")
    public ResponseEntity<Void> testEndAuction(@LoginUser Long userId,
                                               @RequestParam("seconds") int seconds) {
        testService.test(userId, seconds);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
