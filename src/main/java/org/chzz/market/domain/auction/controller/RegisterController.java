package org.chzz.market.domain.auction.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.chzz.market.domain.auction.dto.RegisterRequest;
import org.chzz.market.domain.auction.dto.RegisterResponse;
import org.chzz.market.domain.auction.entity.Auction.AuctionStatus;
import org.chzz.market.domain.auction.service.RegisterService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/register")
public class RegisterController {
    private static final Logger logger = LoggerFactory.getLogger(RegisterController.class);

    private final RegisterService registerService;

    // 경매 등록
    @PostMapping(value = "/auction",
            consumes = {MediaType.MULTIPART_FORM_DATA_VALUE},
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<RegisterResponse> registerAuctionProduct(@ModelAttribute @Valid RegisterRequest request) {
        RegisterResponse response = registerService.register(request, AuctionStatus.PROCEEDING);
        logger.info("경매 등록 상품이 성공적으로 등록되었습니다. 상품 ID: {}", response.productId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 사전 등록
    @PostMapping(value = "/pre-auction",
            consumes = {MediaType.MULTIPART_FORM_DATA_VALUE},
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<RegisterResponse> preRegisterProduct(@ModelAttribute @Valid RegisterRequest request) {
        RegisterResponse response = registerService.register(request, AuctionStatus.PENDING);
        logger.info("사전 등록 상품이 성공적으로 등록되었습니다. 상품 ID: {}", response.productId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
